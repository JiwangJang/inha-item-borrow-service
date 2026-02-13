import Response from "./ResponseInterface";

export default interface RequestInterface {
    id: number;
    item: RequestItem;
    manager: RequestManager | null;
    borrowerId: string;
    borrowerName: string;
    createdAt: Date;
    returnAt: Date;
    borrowAt: Date;
    type: RequestType;
    state: RequestState;
    cancel: boolean | null;
    response: Response | null;
}

export interface RequestItem {
    id: number;
    name: string;
    price: number;
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

export const REQUEST_STATE = {
    PENDING: "PENDING",
    ASSIGNED: "ASSIGNED",
    REJECT: "REJECT",
    PERMIT: "PERMIT",
} as const;

export type RequestState = (typeof REQUEST_STATE)[keyof typeof REQUEST_STATE];
