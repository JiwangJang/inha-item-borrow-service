"use client";

import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import RequestInterface, { REQUEST_STATE_TYPE, REQUEST_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import { notFound, useRouter } from "next/navigation";
import { useContext, useEffect, useState } from "react";
import ItemBorrowConditions from "../../borrower-request/ItemBorrowConditions";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import axios from "axios";
import errorHandler from "@/utilities/errorHandler";
import Loading from "@/components/utilities/Loading";
import BorrowerContext from "@/context/BorrowerContext";
import API_SERVER from "@/apiServer";
import ReturnDateSelector from "@/components/utilities/ReturnDateSelector";
import AlertModal from "@/components/utilities/modal/AlertModal";
import ItemContext from "@/context/ItemContext";
import { ITEM_STATE_TYPE } from "@/types/ItemStateType";

export default function SingleBorrowRequestPage({ requestId }: { requestId: string }) {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [viewPasswordAlertModal, setViewPasswordAlertModal] = useState(false);
    const [returnReviseConfirmModal, setReturnReviseConfirmModal] = useState(false);
    const [requestCancelModal, setRequestCancelModal] = useState(false);
    const [dateSelector, setDateSelector] = useState(false);
    const [remainTime, setRemainTime] = useState<string>("");
    const { requestList, setRequestList } = useContext(BorrowRequestContext);
    const { borrowerInfo } = useContext(BorrowerContext);
    const { itemList, setItemList } = useContext(ItemContext);
    const currentRequest = requestList
        .filter((rq) => rq.type == REQUEST_TYPE.BORROW)
        .find((r) => String(r.id) == requestId);
    if (currentRequest == undefined) {
        notFound();
    }

    const { id, item, borrowAt, returnAt, viewPasswordAt, state, manager, cancel } = currentRequest;
    const { id: itemId, name: itemName, location: itemLocation, password: itemPassword, price: itemPrice } = item;

    const returnRequest = requestList.find(
        (rq) => rq.type == REQUEST_TYPE.RETURN && new Date(rq.borrowAt).getTime() === new Date(borrowAt).getTime(),
    );

    useEffect(() => {
        const today = new Date();
        const borrowAtDate = new Date(borrowAt);
        let interval: NodeJS.Timeout;

        if (viewPasswordAt == null && today >= borrowAtDate) {
            setViewPasswordAlertModal(true);
        } else if (state == REQUEST_STATE_TYPE.PERMIT && viewPasswordAt == null) {
            // 계속 확인
            const now = new Date();
            const remained = borrowAtDate.getTime() - now.getTime();
            const seconds = Math.floor(remained / 1000);

            const minites = Math.floor(seconds / 60);
            const hours = Math.floor(minites / 60);
            const dates = Math.floor(hours / 24);

            const second = seconds % 60;
            const minite = minites % 60;
            const hour = hours % 24;

            setRemainTime(`${dates}일 ${hour}시간 ${minite}분 ${second}초 뒤 공개`);

            interval = setInterval(() => {
                const now = new Date();
                const remained = borrowAtDate.getTime() - now.getTime();
                const seconds = Math.floor(remained / 1000);

                const minites = Math.floor(seconds / 60);
                const hours = Math.floor(minites / 60);
                const dates = Math.floor(hours / 24);

                const second = seconds % 60;
                const minite = minites % 60;
                const hour = hours % 24;

                setRemainTime(`${dates}일 ${hour}시간 ${minite}분 ${second}초 뒤 공개`);

                if (remained <= 0) {
                    setViewPasswordAlertModal(true);
                    clearInterval(interval);
                }
            }, 1000);
        }

        return () => clearInterval(interval);
    }, []);

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

    const returnReviseConfirmModalOn = () => {
        setReturnReviseConfirmModal(true);
    };

    const returnReviseConfirmModalOff = () => {
        setReturnReviseConfirmModal(false);
    };

    const dateSelectorOn = () => {
        setDateSelector(true);
    };

    const dateSelectorOff = () => {
        setDateSelector(false);
    };

    const returnRequestSend = async (returnAtString?: string) => {
        if (setRequestList == null) {
            alert("새로고침후 다시 시도해주세요. 지속적으로 발생할 경우 관리자에게 연락해주세요.");
            return;
        }

        if (state != REQUEST_STATE_TYPE.PERMIT || viewPasswordAt == null) {
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
                returnAt: returnAtString == null ? returnAt : returnAtString,
                borrowAt: borrowAt,
                type: REQUEST_TYPE.RETURN,
            };

            const result = await axios.post(`${API_SERVER}/requests`, body, { withCredentials: true });
            const { data } = result.data;

            const returnRequest: RequestInterface = {
                prevRequestId: Number(requestId),
                id: data.id,
                borrowerName: borrowerInfo!.name,
                cancel: false,
                createdAt: data.createdAt,
                item: item,
                manager: null,
                response: null,
                state: REQUEST_STATE_TYPE.PENDING,
                borrowAt,
                viewPasswordAt: null,
                borrowerId: borrowerInfo!.id,
                returnAt: body.returnAt,
                type: body.type,
            };

            setRequestList(
                requestList.concat(returnRequest).map((rq) => {
                    if (rq.id == id) {
                        return {
                            ...rq,
                            item: {
                                id: itemId,
                                location: null,
                                name: itemName,
                                password: null,
                                price: itemPrice,
                            },
                        };
                    }
                    return rq;
                }),
            );

            alert("반납신청을 완료했습니다.");
            returnReviseConfirmModalOff();
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

    const viewPasswordReq = async () => {
        if (setRequestList == null) {
            alert("새로고침후 다시 시도해주세요. 지속적으로 발생할 경우 관리자에게 연락해주세요.");
            return;
        }
        try {
            const res = await axios.patch(`${API_SERVER}/requests/${id}/view-password`, null, {
                withCredentials: true,
            });
            const { data } = res.data;
            setRequestList(
                requestList.map((rq) => {
                    if (rq.id == id) {
                        return data;
                    }
                    return rq;
                }),
            );
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
    };

    const cancelRequest = async () => {
        if (currentRequest.state != REQUEST_STATE_TYPE.PENDING) {
            alert("관리자 배정 이전에만 취소가 가능합니다.");
            return;
        }

        try {
            await axios.patch(`${API_SERVER}/requests/${requestId}/cancel`, null, { withCredentials: true });

            if (setRequestList) {
                // 취소한 요청 표시
                setRequestList(
                    requestList.map((rq) => {
                        if (String(rq.id) == requestId) {
                            return {
                                ...rq,
                                cancel: true,
                            };
                        }
                        return rq;
                    }),
                );
            }

            if (setItemList) {
                // 취소한 아이템 대여 가능하다는 상태로 보여지게 설정
                setItemList(
                    itemList.map((it) => {
                        if (it.id == currentRequest.item.id) {
                            return {
                                ...it,
                                state: ITEM_STATE_TYPE.AFFORD,
                            };
                        }
                        return it;
                    }),
                );
            }

            alert("해당 요청 취소가 완료됐습니다.");
            router.back();
        } catch (error) {
            console.error(error);
        }
    };

    const goRevisePage = () => {
        if (currentRequest.cancel) {
            alert("취소된 요청은 수정할 수 없습니다.");
            return;
        }
        if (currentRequest.state != REQUEST_STATE_TYPE.PENDING) {
            alert("관리자 배정 이전에만 수정이 가능합니다.");
            return;
        }
        router.push(`/borrow-list/${requestId}/revise`);
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
                <InfoRow
                    label="보관위치"
                    value={
                        state == REQUEST_STATE_TYPE.PERMIT && viewPasswordAt != null
                            ? itemLocation!
                            : state == REQUEST_STATE_TYPE.PERMIT && viewPasswordAt == null
                              ? remainTime
                              : "-"
                    }
                />
                <InfoRow
                    label="비밀번호"
                    value={
                        state == REQUEST_STATE_TYPE.PERMIT && viewPasswordAt != null
                            ? itemPassword!
                            : state == REQUEST_STATE_TYPE.PERMIT && viewPasswordAt == null
                              ? remainTime
                              : "-"
                    }
                />
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
                    disabled={state != REQUEST_STATE_TYPE.PERMIT || viewPasswordAt == null || returnRequest != null}
                    className="py-3 flex-1 bold-16px"
                    onClick={returnReviseConfirmModalOn}
                />
            </div>

            {state != REQUEST_STATE_TYPE.PERMIT ? (
                // 허가가 아니면(PENDING or ASSIGNED or REJECT)
                <p className="mt-2">
                    [알림] 보관위치와 비밀번호는 허가 승인이 난 뒤 대여일시 이후에 확인할 수 있습니다.
                </p>
            ) : null}
            <div className="mt-5">
                <p className="black-20px mb-2">📝물품대여조건</p>
                <ItemBorrowConditions price={itemPrice} />
            </div>

            {/* 수정 취소 버튼 */}
            {state == REQUEST_STATE_TYPE.PENDING && !cancel ? (
                <div className="flex gap-1 mt-3">
                    <Button
                        title="취소하기"
                        className="w-full py-2 bg-white! border-2 border-black text-black!"
                        onClick={() => setRequestCancelModal(true)}
                    />
                    <Button title="수정하기" className="w-full py-2" onClick={goRevisePage} />
                </div>
            ) : (
                <p className="text-center mt-4">담당자 배정 이전에만 수정 또는 취소가 가능합니다.</p>
            )}

            {/* 반납신청시 반납일시 수정할 거냐 물어보는 모달 */}
            <ConfirmModal
                open={returnReviseConfirmModal}
                message={`당초 신청하신 ${dateFormatter(returnAt)}에 반납하시겠습니까?(취소 누르실 경우 날짜와 시간 수정이 가능합니다.)`}
                onConfirm={returnRequestSend}
                onCancel={dateSelectorOn}
                onClose={returnReviseConfirmModalOff}
                title="알림"
            />

            {/* 비밀번호 보게 하는 모달 */}
            <AlertModal
                open={viewPasswordAlertModal}
                message={
                    "해당 물품의 비밀번호와 보관위치를 확인하시겠습니까? 확인버튼을 누르시면 물품을 수령하신 것으로 간주합니다."
                }
                onConfirm={viewPasswordReq}
                onClose={() => setViewPasswordAlertModal(false)}
                title="알림"
            />
            {/* 반납일시 수정할 때 사용하는 선택창 */}
            <ReturnDateSelector
                open={dateSelector}
                onClose={dateSelectorOff}
                sendFunc={returnRequestSend}
                initialReturnAt={returnAt}
            />

            {/* 취소할거냐 물어볼때 쓰는 모달 */}
            <ConfirmModal
                title="알림"
                message="정말 취소하시겠습니까?"
                onClose={() => setRequestCancelModal(false)}
                onConfirm={cancelRequest}
                open={requestCancelModal}
            />
            <Loading open={loading} />
        </div>
    );
}
