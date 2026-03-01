import { AdminPositionType } from "./AdminPositionType";

export default interface NoticeInterface {
    id: number;
    title: string;
    content: string;
    authorId: string;
    postedAt: string;
    updatedAt: string;
    adminName: string;
    adminPosition: AdminPositionType;
}
