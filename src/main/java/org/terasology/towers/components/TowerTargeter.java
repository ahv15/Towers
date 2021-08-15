// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.towers.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all the Targeter blocks.
 * <p>
 * Targeters select the enemies the tower will attack.
 * <p>
 * Provides a number of common properties.
 *
 */
public abstract class TowerTargeter implements Component {
    /**
     * The range of this targeter
     * given in blocks
     */
    public int range;
    /**
     * The time between attacks for this targeter
     * given in ms
     */
    public int attackSpeed;
    /**
     * All enemies hit by an effect last attack
     */
    public Set<EntityRef> affectedEnemies = new HashSet<>();

    /**
     * A balancing multiplier passed to effectors on this tower.
     * It's used to provide balancing between different tower types.
     *
     * @return The multiplier to be passed to all effectors
     */
    public abstract float getMultiplier();

}
