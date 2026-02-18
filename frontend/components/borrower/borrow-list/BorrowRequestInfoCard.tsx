"use client";

import Button from "@/components/utilities/Button";
import { REQUEST_STATE_TYPE, REQUEST_TYPE, RequestStateType, RequestType } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { MouseEvent, useState } from "react";

interface RequestInfoCardInterface {
    requestId: number;
    itemName: string;
    requestAt: string;
    borrowAt: string;
    retrunAt: string;
    state: RequestStateType;
    type: RequestType;
    cancel: boolean;
}

export default function RequestInfoCard({
    requestId,
    itemName,
    requestAt,
    borrowAt,
    retrunAt,
    state,
    type,
    cancel,
}: RequestInfoCardInterface) {
    const [viewDetailButton, setViewDetailButton] = useState(false);
    const isBorrowRequest = type == REQUEST_TYPE.BORROW;
    const router = useRouter();
    let stateString: string;

    switch (state) {
        case REQUEST_STATE_TYPE.ASSIGNED:
            stateString = "검토중(담당자 배정됨)";
            break;
        case REQUEST_STATE_TYPE.PENDING:
            stateString = "검토중(담당자 배정안됨)";
            break;
        case REQUEST_STATE_TYPE.PERMIT:
            stateString = "허가";
            break;
        default:
            stateString = "불허가";
            break;
    }

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
                <p>대여번호 : {requestId}</p>
                <p>대여물품 : {itemName} </p>
                <p>대여일시 : {dateFormatter(borrowAt)} </p>
                <p>
                    반납일시 : {dateFormatter(retrunAt)}
                    {isBorrowRequest ? " (예정)" : ""}
                </p>
                <p>대여상태 : {cancel ? "요청 취소" : stateString}</p>
            </div>
            {viewDetailButton ? (
                <div className="flex gap-1 mt-3">
                    <Button title="닫기" className="py-2.5 flex-1  bold-16px bg-placeholder!" onClick={detailClose} />
                    <Button
                        title="세부사항보기"
                        className="py-2.5 flex-1  bold-16px"
                        onClick={() =>
                            router.push(isBorrowRequest ? `/borrow-list/${requestId}` : `/return-list/${requestId}`)
                        }
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
