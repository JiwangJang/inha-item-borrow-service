import { ItemStateType } from "./ItemStateType";

export default interface ItemInterface {
    id: number;
    name: string;
    location: string;
    price: number;
    password: string;
    deleteReason: string | null;
    state: ItemStateType;
}
