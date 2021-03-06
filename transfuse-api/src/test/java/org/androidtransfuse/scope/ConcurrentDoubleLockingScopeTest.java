/**
 * Copyright 2011-2015 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.scope;

import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author John Ericksen
 */
public class ConcurrentDoubleLockingScopeTest {

    private interface ScopeTargetBuilder extends Provider<ScopeTarget> {
    }

    private ScopeTarget scopeTarget;
    private ScopeTargetBuilder builder;
    private Scope scope;

    @Before
    public void setup() {
        builder = mock(ScopeTargetBuilder.class);
        scope = new ConcurrentDoubleLockingScope();
        scopeTarget = new ScopeTarget();
    }

    @Test
    public void testScopedBuild() {
        when(builder.get()).thenReturn(scopeTarget);

        ScopeTarget resultTarget = scope.getScopedObject(ScopeKey.of(ScopeTarget.class), builder);
        assertEquals(scopeTarget, resultTarget);
        ScopeTarget secondResultTarget = scope.getScopedObject(ScopeKey.of(ScopeTarget.class), builder);
        assertEquals(scopeTarget, secondResultTarget);
    }

    @Test
    public void testKeyScope(){
        class ScopeTargetBuilderImpl implements ScopeTargetBuilder{
            @Override
            public ScopeTarget get() {
                return new ScopeTarget();
            }
        }

        ScopeTarget scoped1 = scope.getScopedObject(ScopeKey.of(ScopeTarget.class), new ScopeTargetBuilderImpl());
        ScopeTarget scoped2 = scope.getScopedObject(ScopeKey.of(ScopeTarget.class), new ScopeTargetBuilderImpl());
        ScopeTarget scoped3 = scope.getScopedObject(ScopeKey.of(ScopeTarget.class), new ScopeTargetBuilderImpl());
        ScopeTarget scoped4 = scope.getScopedObject(ScopeKey.of(ScopeTarget.class).annotatedBy("@test"), new ScopeTargetBuilderImpl());
        ScopeTarget scoped5 = scope.getScopedObject(ScopeKey.of(ScopeTarget.class).annotatedBy("@test"), new ScopeTargetBuilderImpl());
        ScopeTarget scoped6 = scope.getScopedObject(ScopeKey.of(ScopeTarget.class).annotatedBy("@test2"), new ScopeTargetBuilderImpl());

        assertSame(scoped1, scoped2);
        assertSame(scoped1, scoped3);
        assertSame(scoped4, scoped5);
        assertNotSame(scoped1, scoped4);
        assertNotSame(scoped1, scoped4);
        assertNotSame(scoped1, scoped6);
        assertNotSame(scoped5, scoped6);
    }

}
