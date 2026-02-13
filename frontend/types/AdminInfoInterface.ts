import { AdminPositionType } from "./AdminPositionType";

export default interface AdminInfoInterface {
    id: string;
    name: string;
    position: AdminPositionType;
    divisionCode: string;
}
