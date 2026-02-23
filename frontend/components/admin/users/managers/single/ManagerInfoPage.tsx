"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import AdminContext from "@/context/AdminContext";
import AdminListContext from "@/context/AdminListContext";
import { ADMIN_POSITION_TYPE } from "@/types/AdminPositionType";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function ManagerInfoPage({ adminId }: { adminId: string }) {
    const router = useRouter();

    const { adminList, setAdminList } = useContext(AdminListContext);
    const { adminInfo } = useContext(AdminContext);
    const [confirmModal, setConfirmModal] = useState(false);
    const current = adminList.find((admin) => admin.id == adminId);
    if (current == null) {
        notFound();
    }

    const revokeAuthority = async () => {
        try {
            await axios.delete(`${API_SERVER}/admins/sub-admin/${adminId}`, { withCredentials: true });

            if (setAdminList) {
                setAdminList((prev) => prev.filter((admin) => admin.id != adminId));
            }
            alert("권한회수가 완료됐습니다.");
            router.back();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 잠시후 다시 시도해보시고, 지속적으로 발생하는 경우 개발자에게 연락해주세요.");
            return;
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px mb-2">ℹ️ 관리자 정보</p>
            <InfoTable>
                <InfoRow label="이름" value={current.name} />
                <InfoRow label="아이디" value={current.id} />
                <InfoRow label="부서" value={current.divisionCode} />
                <InfoRow label="직급" value={current.position} />
            </InfoTable>
            {adminInfo?.position == ADMIN_POSITION_TYPE.PRESIDENT && current.id != adminInfo?.id ? (
                <div className="flex justify-center mt-4">
                    <Button
                        title="권한회수"
                        onClick={() => setConfirmModal(true)}
                        className="py-3 px-6 bg-alert! bold-18px"
                    />
                </div>
            ) : null}
            <ConfirmModal
                open={confirmModal}
                message={`${current.name}(${current.id})의 권한을 회수하시겠습니까?`}
                onClose={() => setConfirmModal(false)}
                onConfirm={revokeAuthority}
                title="경고"
            />
        </div>
    );
}
