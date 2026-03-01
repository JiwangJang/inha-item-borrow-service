import { ITEM_STATE_TYPE, ItemStateType } from "@/types/ItemStateType";

export default function itemStateTypeConvertor(statusType: ItemStateType) {
    switch (statusType) {
        case ITEM_STATE_TYPE.AFFORD:
            return "대여가능";
        case ITEM_STATE_TYPE.DELETED:
            return "삭제됨";
        case ITEM_STATE_TYPE.BORROWED:
            return "대여중";
        default:
            return "심사중(반납 또는 신청)";
    }
}
