import { ItemStatusType } from "./ItemStatusType";

export default interface ItemInterface {
    id: number;
    name: string;
    location: string | null;
    price: number;
    password: string | null;
    deleteReason: string | null;
    status: ItemStatusType;
}
