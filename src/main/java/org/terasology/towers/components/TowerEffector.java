// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.towers.components;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.towers.EffectCount;
import org.terasology.towers.EffectDuration;

/**
 * Base class for all the Effector blocks.
 * <p>
 * Effectors apply damage and special effects to the enemies.
 * @see TowerTargeter
 */
public abstract class TowerEffector<T extends TowerEffector> implements Component<T> {
    /**
     * Controls how often the effect should be applied on a more abstract level.
     *
     * @return The amount of times that this effect should be applied
     */
    public abstract EffectCount getEffectCount();

    /**
     * Controls how the effect should be called to be removed.
     *
     * @return How long the effect is intended to last.
     * @see EffectDuration
     */
    public abstract EffectDuration getEffectDuration();
}
