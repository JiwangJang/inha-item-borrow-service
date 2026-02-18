"use client";

import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import RequestInterface, { REQUEST_STATE_TYPE, REQUEST_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";
import ItemBorrowConditions from "../../borrower-request/ItemBorrowConditions";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import axios from "axios";
import errorHandler from "@/utilities/errorHandler";
import Loading from "@/components/utilities/Loading";
import BorrowerContext from "@/context/BorrowerContext";
import API_SERVER from "@/apiServer";
import ReturnDateSelector, { toKstOffsetDateTimeString } from "@/components/utilities/ReturnDateSelector";

export default function SingleBorrowRequestPage({ requestId }: { requestId: string }) {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [confirmModal, setConfirmModal] = useState(false);
    const [dateSelector, setDateSelector] = useState(false);
    const { requestList, setRequestList } = useContext(BorrowRequestContext);
    const { borrowerInfo } = useContext(BorrowerContext);
    const currentRequest = requestList
        .filter((rq) => rq.type == REQUEST_TYPE.BORROW)
        .find((r) => String(r.id) == requestId);
    if (currentRequest == undefined) {
        notFound();
    }

    const { id, item, borrowAt, returnAt, state, manager, cancel } = currentRequest;
    const { id: itemId, name: itemName, location: itemLocation, password: itemPassword, price: itemPrice } = item;

    let borrowState;

    switch (state) {
        case REQUEST_STATE_TYPE.ASSIGNED:
            borrowState = `검토중(담당자 : ${manager!.name})`;
            break;
        case REQUEST_STATE_TYPE.PENDING:
            borrowState = "검토중(담당자 배정안됨)";
            break;
        case REQUEST_STATE_TYPE.PERMIT:
            borrowState = "허가승인";
            break;
        default:
            borrowState = "불허가";
            break;
    }

    const confirmModalOn = () => {
        setConfirmModal(true);
    };

    const confirmModalOff = () => {
        setConfirmModal(false);
    };

    const dateSelectorOn = () => {
        setDateSelector(true);
    };

    const dateSelectorOff = () => {
        setDateSelector(false);
    };

    const returnRequestSend = async (returnAtString?: string) => {
        // borrowAt: `${borrowDate}T${borrowTime}:00+09:00`, 이런식으로 하면 됨

        if (setRequestList == null) {
            alert("새로고침후 다시 시도해주세요. 지속적으로 발생할 경우 관리자에게 연락해주세요.");
            return;
        }

        if (returnAtString != null && new Date() > new Date(returnAtString)) {
            // 반납일시가 대여 일시보다 이전이거나, 현재보다 이전인 경우 요청 못보냄
            alert("올바른 반납일시를 선택해주세요.");
            return;
        }

        setLoading(true);
        try {
            const body = {
                prevRequestId: id,
                itemId,
                borrowerId: borrowerInfo?.id,
                returnAt: returnAtString == null ? toKstOffsetDateTimeString(Number(returnAt)) : returnAtString,
                borrowAt: toKstOffsetDateTimeString(Number(borrowAt)),
                type: REQUEST_TYPE.RETURN,
            };

            const result = await axios.post(`${API_SERVER}/requests`, body, { withCredentials: true });
            const { data } = result.data;

            const returnRequest: RequestInterface = {
                prevRequestId: Number(requestId),
                id: data.requestId,
                borrowerName: borrowerInfo!.name,
                cancel: false,
                createdAt: data.createdAt,
                item: item,
                manager: null,
                response: null,
                state: REQUEST_STATE_TYPE.PENDING,
                borrowAt,
                borrowerId: borrowerInfo!.id,
                returnAt: body.returnAt,
                type: body.type,
            };

            setRequestList(requestList.concat(returnRequest));

            alert("반납신청을 완료했습니다.");
            confirmModalOff();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert(
                "알 수 없는 에러가 발생했습니다. 다시 한 번 시도해보시고, 지속적으로 발생할 경우 관리자에게 연락해주세요.",
            );
            console.error(error);
        }
        setLoading(false);
    };

    return (
        <div className="mt-5">
            <p className="black-20px mb-3">✅ 기본정보</p>
            <InfoTable>
                <InfoRow label="요청번호" value={String(id)} />
                <InfoRow label="대여물품" value={itemName} />
                <InfoRow label="대여일시" value={dateFormatter(borrowAt)} />
                <InfoRow label="반납일시" value={`${dateFormatter(returnAt)}(예정)`} />
                <InfoRow label="요청상태" value={cancel ? "요청취소" : borrowState} />
                <InfoRow label="보관위치" value={itemLocation ?? "-"} />
                <InfoRow label="비밀번호" value={itemPassword ?? "-"} />
                {state == REQUEST_STATE_TYPE.REJECT ? (
                    // 불허가면
                    <InfoRow label="거절사유" value={currentRequest.response!.rejectReason} />
                ) : null}
            </InfoTable>

            <div className="flex gap-2 pt-4">
                <Button
                    title="대여신청서 확인"
                    className="py-3 flex-1 regular-16px bg-white! text-black! border border-black!"
                    onClick={() => router.push(`/borrow-list/${id}/paper`)}
                />
                <Button
                    title="반납신청하기"
                    disabled={state != REQUEST_STATE_TYPE.PERMIT}
                    className="py-3 flex-1 bold-16px"
                    onClick={confirmModalOn}
                />
            </div>

            {state != REQUEST_STATE_TYPE.PERMIT ? (
                // 허가가 아니면(PENDING or ASSIGNED or REJECT)
                <p className="mt-2">[알림] 보관위치와 비밀번호는 허가 승인후 나타납니다.</p>
            ) : null}
            <div className="mt-5">
                <p className="black-20px mb-2">📝물품대여조건</p>
                <ItemBorrowConditions price={itemPrice} />
            </div>

            <ConfirmModal
                open={confirmModal}
                message={`당초 신청하신 ${dateFormatter(returnAt)}에 반납하시겠습니까? `}
                onConfirm={returnRequestSend}
                onCancel={dateSelectorOn}
                onClose={confirmModalOff}
                title="알림"
            />
            <ReturnDateSelector
                open={dateSelector}
                onClose={dateSelectorOff}
                sendFunc={returnRequestSend}
                initialReturnAt={returnAt}
            />
            <Loading open={loading} />
        </div>
    );
}
