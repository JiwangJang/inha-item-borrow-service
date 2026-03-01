"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import PromptModal from "@/components/utilities/modal/PromptModal";
import AdminContext from "@/context/AdminContext";
import SearchedBorrowersContext from "@/context/SearchedContext";
import { ADMIN_POSITION_TYPE } from "@/types/AdminPositionType";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { notFound } from "next/navigation";
import { useContext, useState } from "react";

export default function SearchedBorrowerPage({ borrowerId }: { borrowerId: string }) {
    const { adminInfo } = useContext(AdminContext);
    const { searchedBorrowers, setSearchedBorrowers } = useContext(SearchedBorrowersContext);
    const current = searchedBorrowers.find(({ id }) => id == borrowerId);
    const [promptModalOn, setPromptModalOn] = useState(false);
    const [confirmModalOn, setConfirmModalOn] = useState(false);

    if (current == null) {
        notFound();
    }

    const { id, name, accountNumber, ban, department, phoneNumber, verify, banReason } = current!;

    const banFunc = async (banReason: string) => {
        if (setSearchedBorrowers == null) {
            alert("새로고침 후 다시 시도해주세요.");
            return;
        }
        try {
            const body = {
                ban: true,
                banReason,
            };

            await axios.patch(`${API_SERVER}/borrowers/${id}/info/ban`, body, { withCredentials: true });

            setSearchedBorrowers((prev) =>
                prev.map((p) => {
                    if (p.id == id) {
                        return {
                            ...p,
                            ban: true,
                            banReason,
                        };
                    }
                    return p;
                }),
            );
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 잠시후 다시 시도해보시고, 지속될경우 개발자에게 알려주세요.");
            console.error(error);
            return;
        }
    };

    const cancelBanFunc = async () => {
        if (setSearchedBorrowers == null) {
            alert("새로고침 후 다시 시도해주세요.");
            return;
        }
        try {
            // 여기 해제하는거
            const body = {
                ban: false,
            };
            await axios.patch(`${API_SERVER}/borrowers/${id}/info/ban`, body, { withCredentials: true });
            setSearchedBorrowers((prev) =>
                prev.map((p) => {
                    if (p.id == id) {
                        return {
                            ...p,
                            ban: false,
                            banReason: null,
                        };
                    }
                    return p;
                }),
            );
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 잠시후 다시 시도해보시고, 지속될경우 개발자에게 알려주세요.");
            console.error(error);
            return;
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px mb-2">ℹ️ 대여자 정보</p>
            <InfoTable>
                <InfoRow label="이름" value={name} />
                <InfoRow label="학번" value={id} />
                <InfoRow label="학과" value={department} />
                <InfoRow label="반환계좌" value={accountNumber} />
                <InfoRow label="이용금지" value={ban ? "이용금지" : "이용가능"} />
                {banReason != null ? <InfoRow label="금지사유" value={banReason} /> : null}
                <InfoRow label="전화번호" value={phoneNumber} />
                <InfoRow label="회비납부" value={verify ? "납부확인" : "납부미확인"} />
            </InfoTable>
            {adminInfo?.position == ADMIN_POSITION_TYPE.PRESIDENT ? (
                ban ? (
                    <Button
                        title="이용금지 해제"
                        className="w-full py-3 bold-18px mt-2 "
                        onClick={() => setConfirmModalOn(true)}
                    />
                ) : (
                    <Button
                        title="이용금지"
                        className="w-full py-3 bold-18px mt-2 bg-alert!"
                        onClick={() => setPromptModalOn(true)}
                    />
                )
            ) : null}
            <PromptModal
                open={promptModalOn}
                onClose={() => setPromptModalOn(false)}
                onConfirm={(v) => banFunc(v)}
                placeholder="금지 사유를 입력해주세요."
                title="경고"
            />
            <ConfirmModal
                open={confirmModalOn}
                message={`${name}(${id})의 이용금지조치를 해제하시겠습니까?`}
                title="알림"
                onClose={() => setConfirmModalOn(false)}
                onConfirm={cancelBanFunc}
            />
        </div>
    );
}
