package com.ewized.wands.types;

import com.ewized.wands.types.elements.IceWand;
import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WandTypeTest {
    private class User implements Subject {
        @Override
        public Optional<CommandSource> getCommandSource() {
            return null;
        }

        @Override
        public SubjectCollection getContainingCollection() {
            return null;
        }

        @Override
        public SubjectData getSubjectData() {
            return null;
        }

        @Override
        public SubjectData getTransientSubjectData() {
            return null;
        }

        @Override
        public boolean hasPermission(Set<Context> contexts, String permission) {
            return permission.contains("wands.");
        }

        @Override
        public Tristate getPermissionValue(Set<Context> contexts, String permission) {
            return null;
        }

        @Override
        public boolean isChildOf(Set<Context> contexts, Subject parent) {
            return false;
        }

        @Override
        public List<Subject> getParents(Set<Context> contexts) {
            return null;
        }

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public Set<Context> getActiveContexts() {
            return null;
        }
    }

    @Test
    public void permissionTest() {
        User user = new User();
        Assert.assertTrue(new WandType("ice", "name", new IceWand()).hasPermission(user));
    }
}
