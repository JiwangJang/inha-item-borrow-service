"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import AdminContext from "@/context/AdminContext";
import AdminRequestContext from "@/context/AdminRequestContext";
import { REQUEST_STATE_TYPE, REQUEST_TYPE, RequestType } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import Image from "next/image";
import { MouseEvent, useContext, useState } from "react";

interface RequestInfoCardInterface {
    requestId: number;
    itemName: string;
    requesterId: string;
    requesterName: string;
    requestAt: string;
    borrowAt: string;
    retrunAt: string;
    type: RequestType;
}

export default function AdminRequestInfoCard({
    requestId,
    itemName,
    requesterId,
    requesterName,
    requestAt,
    borrowAt,
    retrunAt,
    type,
}: RequestInfoCardInterface) {
    const [viewDetailButton, setViewDetailButton] = useState(false);
    const [confirmModal, setConfirmModal] = useState(false);
    const { requestList, setRequestList } = useContext(AdminRequestContext);
    const { adminInfo } = useContext(AdminContext);

    const isBorrowRequest = type == REQUEST_TYPE.BORROW;

    const detailOpen = () => {
        setViewDetailButton(true);
    };

    const detailClose = (e: MouseEvent<HTMLButtonElement>) => {
        // 부모 div에 클릭이벤트가 전파되는 것을 방지
        e.stopPropagation();
        setViewDetailButton(false);
    };

    const takeRequest = async () => {
        if (setRequestList == null) {
            alert("새로고침 후 시도해주세요.");
            return;
        }

        try {
            await axios.patch(`${API_SERVER}/requests/${requestId}/manage`, null, { withCredentials: true });
            setRequestList(
                requestList.map((rq) => {
                    if (rq.id == requestId) {
                        return {
                            ...rq,
                            manager: {
                                id: adminInfo!.id,
                                name: adminInfo!.name,
                                position: adminInfo!.position,
                            },
                            state: REQUEST_STATE_TYPE.ASSIGNED,
                        };
                    }
                    return rq;
                }),
            );

            alert("배정이 완료 됐습니다. 결과처리에서 확인 하실수 있습니다.");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert(
                "알 수 없는 에러입니다. 새로고침 후 다시 시도해보시고 그래도 계속되면 개발자에게 연락해주세요.(Not Axios)",
            );
        }
    };

    return (
        <div
            className="pt-4 pb-2 px-5 bg-white border border-boxBorder rounded-2xl"
            style={{
                cursor: viewDetailButton ? "" : "pointer",
            }}
            onClick={detailOpen}
        >
            <div className="flex justify-between items-center mb-2">
                <p className="bold-20px">{isBorrowRequest ? "물품대여신청" : "물품반납신청"}</p>
                <p className="text-placeholder regular-12px">{dateFormatter(requestAt)} 신청</p>
            </div>
            <div className="flex flex-col gap-1 regular-16px">
                <SameSpaceRow label="대여번호" value={String(requestId)} />
                <SameSpaceRow label="대여자" value={`${requesterName}(${requesterId})`} />
                <SameSpaceRow label="대여물품" value={itemName} />
                <SameSpaceRow label="대여일시" value={dateFormatter(borrowAt)} />
                <SameSpaceRow
                    label="반납일시"
                    value={isBorrowRequest ? `${dateFormatter(retrunAt)} (예정)` : dateFormatter(retrunAt)}
                />
            </div>
            {viewDetailButton ? (
                <div className="flex gap-1 mt-3">
                    <Button title="닫기" className="py-2.5 flex-1  bold-16px bg-placeholder!" onClick={detailClose} />
                    <Button
                        title="배정받기"
                        className="py-2.5 flex-1  bold-16px"
                        onClick={() => setConfirmModal(true)}
                    />
                </div>
            ) : (
                <div className="flex justify-center">
                    <Image
                        src={"/images/icons/others/active/keyboard_arrow_down.svg"}
                        width={24}
                        height={24}
                        alt="버튼보기 버튼"
                    />
                </div>
            )}
            <ConfirmModal
                open={confirmModal}
                onClose={() => setConfirmModal(false)}
                message="해당 요청을 담당하시겠습니까?"
                onConfirm={takeRequest}
                title="알림"
            />
        </div>
    );
}
