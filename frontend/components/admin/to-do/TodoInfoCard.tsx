"use client";

import Button from "@/components/utilities/Button";
import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import { REQUEST_STATE_TYPE, REQUEST_TYPE, RequestStateType, RequestType } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import Image from "next/image";
import { useRouter } from "next/navigation";
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
    state: RequestStateType;
}

export default function TodoInfoCard({
    requestId,
    itemName,
    requesterId,
    requesterName,
    requestAt,
    borrowAt,
    retrunAt,
    type,
    state,
}: RequestInfoCardInterface) {
    const router = useRouter();
    const [viewDetailButton, setViewDetailButton] = useState(false);

    const isBorrowRequest = type == REQUEST_TYPE.BORROW;

    const detailOpen = () => {
        setViewDetailButton(true);
    };

    const detailClose = (e: MouseEvent<HTMLButtonElement>) => {
        // 부모 div에 클릭이벤트가 전파되는 것을 방지
        e.stopPropagation();
        setViewDetailButton(false);
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
                {state != REQUEST_STATE_TYPE.ASSIGNED ? (
                    <SameSpaceRow label="요청상태" value={state == REQUEST_STATE_TYPE.PERMIT ? "허가" : "불허가"} />
                ) : (
                    <SameSpaceRow label="요청상태" value={"처리중"} />
                )}
            </div>
            {viewDetailButton ? (
                <div className="flex gap-1 mt-3">
                    <Button title="닫기" className="py-2.5 flex-1  bold-16px bg-placeholder!" onClick={detailClose} />
                    <Button
                        title="세부사항보기"
                        className="py-2.5 flex-1  bold-16px"
                        onClick={() => router.push(`/admin/to-do/${requestId}`)}
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
        </div>
    );
}
