package io.github.shanastasia06.productcatalogservice.product.domain;

/*
 * DRAFT status could be changed to ACTIVE & ARCHIVED
 * ACTIVE status could be changed to ARCHIVED
 * Only DRAFT could be deleted
 * Only DRAFT and ACTIVE could be edited
 */
public enum ProductStatus {
    DRAFT, ACTIVE, ARCHIVED;

    public boolean canBeEdited() {
        return this != ARCHIVED;
    }

    public boolean canBeDeleted() {
        return this == DRAFT;
    }

    public boolean canBeActivated() {
        return this == DRAFT;
    }

    public boolean canBeArchived() {
        return this != ARCHIVED;
    }
}
