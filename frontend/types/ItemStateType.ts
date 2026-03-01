export const ITEM_STATE_TYPE = {
    AFFORD: "AFFORD",
    REVIEWING: "REVIEWING",
    BORROWED: "BORROWED",
    DELETED: "DELETED",
} as const;

export type ItemStateType = (typeof ITEM_STATE_TYPE)[keyof typeof ITEM_STATE_TYPE];
