"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import AdminContext from "@/context/AdminContext";
import DivisionContext from "@/context/DivisionContext";
import positionConvertor from "@/utilities/positionConvertor";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function ManagerMyPage() {
    const router = useRouter();
    const { adminInfo, setAdminInfo } = useContext(AdminContext);
    if (adminInfo == null) {
        return;
    }

    const { id, name, divisionCode, position } = adminInfo;
    const { divisionList } = useContext(DivisionContext)!;
    const [confirmModal, setConfirmModal] = useState(false);

    const current = divisionList.find((d) => d.code == divisionCode);

    return (
        <div className="mt-5">
            <p className="black-20px mb-2">👨‍💼 내 정보</p>
            <InfoTable>
                <InfoRow label="아이디" value={id} />
                <InfoRow label="이름" value={name} />
                <InfoRow label="직급" value={positionConvertor(position)} />
                <InfoRow label="부서" value={current!.name} />
            </InfoTable>
            <Button
                className="mt-2 py-3 w-full bold-18px"
                title="비밀번호 변경"
                onClick={() => router.push("/admin/users/managers/me/password")}
            />
            <Button
                className="mt-2 py-3 w-full bg-white! border border-black! text-black! bold-18px"
                title="로그아웃"
                onClick={() => setConfirmModal(true)}
            />
            <ConfirmModal
                open={confirmModal}
                title="알림"
                message="로그아웃 하시겠어요?"
                onClose={() => setConfirmModal(false)}
                onConfirm={async () => {
                    await axios.get(`${API_SERVER}/logout`, { withCredentials: true });
                    alert("로그아웃이 완료됐습니다.");
                    if (setAdminInfo) {
                        setAdminInfo(null);
                    }
                    router.push("/admin-login");
                }}
            />
        </div>
    );
}
