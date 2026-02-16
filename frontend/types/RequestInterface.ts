import Response from "./ResponseInterface";

export default interface RequestInterface {
    id: number;
    item: RequestItem;
    manager: RequestManager | null;
    borrowerId: string;
    borrowerName: string;
    createdAt: string;
    returnAt: string;
    borrowAt: string;
    type: RequestType;
    state: RequestStateType;
    cancel: boolean;
    response: Response | null;
}

export interface RequestItem {
    id: number;
    name: string;
    price: number;
    location: string | null;
    password: string | null;
}

export interface RequestManager {
    id: string;
    name: string;
    position: string;
}

export const REQUEST_TYPE = {
    BORROW: "BORROW",
    RETURN: "RETURN",
} as const;

export type RequestType = (typeof REQUEST_TYPE)[keyof typeof REQUEST_TYPE];

export const REQUEST_STATE_TYPE = {
    PENDING: "PENDING",
    ASSIGNED: "ASSIGNED",
    REJECT: "REJECT",
    PERMIT: "PERMIT",
} as const;

export type RequestStateType = (typeof REQUEST_STATE_TYPE)[keyof typeof REQUEST_STATE_TYPE];
