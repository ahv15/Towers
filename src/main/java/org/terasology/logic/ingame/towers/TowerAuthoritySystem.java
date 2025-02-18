/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.ingame.towers;

import org.terasology.alterationEffects.damageOverTime.DamageOverTimeAlterationEffect;
import org.terasology.assets.management.AssetManager;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;
import org.terasology.world.block.items.OnBlockToItem;

import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TowerAuthoritySystem extends BaseComponentSystem {
    @In
    private AssetManager assetManager;
    @In
    private EntityManager entityManager;
    @In
    private DelayManager delayManager;
    @In
    private PlayerManager playerManager;
    @In
    private Context context;

    @ReceiveEvent
    public void onSettingsChanged(ActivateTowerRequest event, EntityRef player) {
        EntityRef towerEntity = event.towerEntity;
        TowerComponent towerComponent = towerEntity.getComponent(TowerComponent.class);
        towerComponent.isActivated = event.isActivated;
        if (event.isActivated) {
            if (towerComponent.childEntity.equals(EntityRef.NULL)) {
                activateTower(towerEntity, towerComponent);
            }
        } else {
            deactivateTower(towerEntity, towerComponent);
        }
        towerEntity.saveComponent(towerComponent);
    }

    @ReceiveEvent(components = {BlockItemComponent.class})
    public void onItemToBlock(OnBlockItemPlaced event, EntityRef itemEntity, TowerComponent towerComponent) {
        EntityRef towerEntity = event.getPlacedBlock();
        if (towerComponent.isActivated && towerComponent.childEntity.equals(EntityRef.NULL)) {
            activateTower(towerEntity, towerComponent);
        }
        towerEntity.addOrSaveComponent(towerComponent);
    }

    @ReceiveEvent
    public void onBlockToItem(OnBlockToItem event, EntityRef blockEntity, TowerComponent towerComponent) {
        deactivateTower(blockEntity, towerComponent);
        event.getItem().addOrSaveComponent(towerComponent);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH, components = {TowerComponent.class, BlockComponent.class})
    public void onTowerActivated(OnActivatedComponent event, EntityRef towerEntity, TowerComponent towerComponent) {
        if (towerComponent.isActivated && towerComponent.childEntity.equals(EntityRef.NULL)) {
            activateTower(towerEntity, towerComponent);
        }
    }

    @ReceiveEvent
    public void onPeriodicActionTriggered(PeriodicActionTriggeredEvent event, EntityRef towerEntity,
                                          TowerComponent towerComponent, LocationComponent locationComponent) {
        if (getActionId(towerEntity.getId()).equals(event.getActionId())) {
            damageEnemiesInRange(towerEntity, locationComponent, towerComponent, playerManager.getAliveCharacters());
        }
    }

    private void activateTower(EntityRef towerEntity, TowerComponent towerComponent) {
        Prefab rookPrefab = assetManager.getAsset("Towers:testRook", Prefab.class).get();
        EntityBuilder rookEntityBuilder = entityManager.newBuilder(rookPrefab);
        rookEntityBuilder.setOwner(towerEntity);
        rookEntityBuilder.setPersistent(false);
        EntityRef rook = rookEntityBuilder.build();
        towerComponent.childEntity = rook;
        Location.attachChild(towerEntity, rook, new Vector3f(0, 1, 0), new Quat4f(Quat4f.IDENTITY));

        if (!delayManager.hasPeriodicAction(towerEntity, getActionId(towerEntity.getId()))) {
            delayManager.addPeriodicAction(towerEntity, getActionId(towerEntity.getId()),1000,1000);
        }
    }

    private void deactivateTower(EntityRef towerEntity, TowerComponent towerComponent) {
        towerComponent.childEntity.destroy();
        towerComponent.childEntity = EntityRef.NULL;

        if (delayManager.hasPeriodicAction(towerEntity, getActionId(towerEntity.getId()))) {
            delayManager.cancelPeriodicAction(towerEntity, getActionId(towerEntity.getId()));
        }
    }

    private String getActionId(long id) {
        return "targetEnemies_" + id;
    }

    private void damageEnemiesInRange(EntityRef towerEntity, LocationComponent locationComponent, TowerComponent towerComponent,
                                      Set<EntityRef> aliveCharacters) {
        float rangeSqrd = towerComponent.range * towerComponent.range;
        Vector3f towerLocation = locationComponent.getWorldPosition();
        for (EntityRef aliveCharacter: aliveCharacters) {
            Vector3f playerLocation = aliveCharacter.getComponent(LocationComponent.class).getWorldPosition();
            if (playerLocation.distanceSquared(towerLocation) <= rangeSqrd) {
                DamageOverTimeAlterationEffect dotEffect = new DamageOverTimeAlterationEffect(context);
                dotEffect.applyEffect(towerEntity, aliveCharacter, 10, 1000);
            }
        }
    }
}
