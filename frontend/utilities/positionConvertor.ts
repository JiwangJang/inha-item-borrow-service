import { ADMIN_POSITION_TYPE, AdminPositionType } from "@/types/AdminPositionType";

export default function positionConvertor(position: AdminPositionType) {
    switch (position) {
        case ADMIN_POSITION_TYPE.PRESIDENT:
            return "학생회장";
        case ADMIN_POSITION_TYPE.VICE_PRESIDENT:
            return "학생부회장";
        case ADMIN_POSITION_TYPE.DIVISION_HEAD:
            return "국장";
        default:
            return "일반국원";
    }
}
