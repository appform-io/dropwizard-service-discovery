package io.appform.dropwizard.discovery.bundle.id;

public enum IdType {

    DEFAULT {
        @Override
        public <T> T accept(IdTypeVisitor<T> visitor) {
            return visitor.visitDefault();
        }
    },
    COMPACT {
        @Override
        public <T> T accept(IdTypeVisitor<T> visitor) {
            return visitor.visitCompact();
        }
    };

    public abstract <T> T accept(IdType.IdTypeVisitor<T> visitor);

    public interface IdTypeVisitor<T> {

        T visitDefault();

        T visitCompact();

    }
}
