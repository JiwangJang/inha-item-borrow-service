export const ITEM_STATUS_TYPE = {
    AFFORD: "AFFORD",
    REVIEWING: "REVIEWING",
    BORROWED: "BORROWED",
    DELETED: "DELETED",
} as const;

export type ItemStatusType = (typeof ITEM_STATUS_TYPE)[keyof typeof ITEM_STATUS_TYPE];
