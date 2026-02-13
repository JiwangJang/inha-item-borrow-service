export default interface Response {
    id: number;
    requestId: number;
    createdAt: Date;
    rejectReason: string;
    type: ResponseType;
}

export const RESPONSE_TYPE = {
    AFFORD: "BORROW",
    REVIEWING: "RETURN",
} as const;

export type ResponseType = (typeof RESPONSE_TYPE)[keyof typeof RESPONSE_TYPE];
