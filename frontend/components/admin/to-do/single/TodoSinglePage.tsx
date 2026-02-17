"use client";

import API_SERVER from "@/apiServer";
import LoginRequired from "@/components/borrower/LoginRequired";
import Button from "@/components/utilities/Button";
import Loading from "@/components/utilities/Loading";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import PromptModal from "@/components/utilities/modal/PromptModal";
import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import AdminRequestContext from "@/context/AdminRequestContext";
import { REQUEST_STATE_TYPE, REQUEST_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { useContext, useState } from "react";

export default function TodoSinglePage({ id }: { id: string }) {
    const { requestList, setRequestList } = useContext(AdminRequestContext);
    const current = requestList.find((rq) => String(rq.id) == id);
    if (current == null) {
        return <LoginRequired />;
    }

    const [loading, setLoading] = useState<boolean>(false);
    const [confirmModalContent, setConfirmModalContent] = useState<string>("");
    const [confirmModal, setConfirmModal] = useState<boolean>(false);

    const [promptModal, setPromptModal] = useState<boolean>(false);

    const [onConfirm, setOnConfirm] = useState<() => void>(() => {});
    const [onPromptSubmit, setOnPromptSubmit] = useState<(e: string) => void>(() => {});

    const {
        id: requestId,
        item,
        borrowerId,
        borrowerName,
        createdAt: requestAt,
        returnAt,
        borrowAt,
        type,
        state,
    } = current;
    const { name: itemName } = item;
    const isBorrowRequest = type == REQUEST_TYPE.BORROW;
    const isPermitedRequest = state == REQUEST_STATE_TYPE.PERMIT;

    const rejectFunc = async (rejectReason: string) => {
        if (setRequestList == null) {
            alert("새로고침 후 다시 시도해주세요");
            return;
        }

        if (rejectReason == "") {
            alert("거절 사유는 필수입니다.");
            return;
        }

        try {
            const body = {
                requestId,
                rejectReason,
                type,
            };

            const result = await axios.post(`${API_SERVER}/responses`, body, { withCredentials: true });
            const { data } = result.data;

            setRequestList(
                requestList.map((rq) => {
                    if (rq.id == requestId) {
                        return {
                            ...rq,
                            response: data,
                            state: REQUEST_STATE_TYPE.REJECT,
                        };
                    }

                    return rq;
                }),
            );

            alert("응답 등록이 완료됐습니다.");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 다시 시도해보시고, 지속될 경우 관리자에게 연락해주세요.(Axios 아님)");
            console.error(error);
        }
    };

    const permitFunc = async () => {
        if (setRequestList == null) {
            alert("새로고침 후 다시 시도해주세요");
            return;
        }

        try {
            const body = {
                requestId,
                type,
            };

            const result = await axios.post(`${API_SERVER}/responses`, body, { withCredentials: true });
            const { data } = result.data;

            setRequestList(
                requestList.map((rq) => {
                    if (rq.id == requestId) {
                        return {
                            ...rq,
                            response: data,
                            state: REQUEST_STATE_TYPE.PERMIT,
                        };
                    }

                    return rq;
                }),
            );

            alert("응답 등록이 완료됐습니다.");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 다시 시도해보시고, 지속될 경우 관리자에게 연락해주세요.(Axios 아님)");
            console.error(error);
        }
    };

    const modifyToPermit = async () => {
        if (setRequestList == null) {
            alert("새로고침 후 다시 시도해주세요");
            return;
        }

        try {
            const body = {
                requestId,
                requestState: REQUEST_STATE_TYPE.PERMIT,
            };

            await axios.patch(`${API_SERVER}/responses/${current.response?.id}`, body, {
                withCredentials: true,
            });

            setRequestList(
                requestList.map((rq) => {
                    if (rq.id == requestId) {
                        return {
                            ...rq,
                            response: {
                                ...rq.response!,
                                createdAt: new Date().toISOString(),
                            },
                            state: REQUEST_STATE_TYPE.PERMIT,
                        };
                    }

                    return rq;
                }),
            );

            alert("응답 수정이 완료됐습니다.");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 다시 시도해보시고, 지속될 경우 관리자에게 연락해주세요.(Axios 아님)");
            console.error(error);
        }
    };

    const modifyToReject = async (rejectReason: string) => {
        if (setRequestList == null) {
            alert("새로고침 후 다시 시도해주세요");
            return;
        }

        if (rejectReason == "") {
            alert("거절 사유는 필수입니다.");
            return;
        }

        try {
            const body = {
                requestId,
                rejectReason,
                requestState: REQUEST_STATE_TYPE.REJECT,
            };

            await axios.patch(`${API_SERVER}/responses/${current.response?.id}`, body, {
                withCredentials: true,
            });

            setRequestList(
                requestList.map((rq) => {
                    if (rq.id == requestId) {
                        return {
                            ...rq,
                            response: {
                                ...rq.response!,
                                rejectReason,
                                createdAt: new Date().toISOString(),
                            },
                            state: REQUEST_STATE_TYPE.REJECT,
                        };
                    }

                    return rq;
                }),
            );

            alert("응답 수정이 완료됐습니다.");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 다시 시도해보시고, 지속될 경우 관리자에게 연락해주세요.(Axios 아님)");
            console.error(error);
        }
    };

    const notAllowBtnOnclick = () => {
        setConfirmModalContent("이 요청을 불허가하시겠습니까?");
        setConfirmModal(true);

        setOnConfirm(() => () => {
            setConfirmModal(false);
            setPromptModal(true);
            setOnPromptSubmit(() => (v: string) => {
                setLoading(true);

                isPermitedRequest
                    ? modifyToReject(v).finally(() => {
                          setLoading(false);
                          setPromptModal(false);
                      })
                    : rejectFunc(v).finally(() => {
                          setLoading(false);
                          setPromptModal(false);
                      });
            });
        });
    };

    const allowBtnOnclick = () => {
        setConfirmModalContent("이 요청을 허가하시겠습니까?");
        setConfirmModal(true);

        setOnConfirm(() => () => {
            setConfirmModal(false);
            setLoading(true);
            state == REQUEST_STATE_TYPE.REJECT
                ? modifyToPermit().finally(() => {
                      setLoading(false);
                  })
                : permitFunc().finally(() => {
                      setLoading(false);
                  });
        });
    };
    return (
        <>
            <div className="mt-5 bg-white rounded-xl border-boxBorder border pt-4 pb-5 px-4">
                <p className="bold-20px text-center mb-4">{isBorrowRequest ? "물품대여신청서" : "물품반납신청서"}</p>
                <div className="flex flex-col gap-1 regular-16px mb-6">
                    <SameSpaceRow label="대여번호" value={String(requestId)} />
                    <SameSpaceRow label="대여자" value={`${borrowerName}(${borrowerId})`} />
                    <SameSpaceRow label="대여물품" value={itemName} />
                    <SameSpaceRow label="대여일시" value={dateFormatter(borrowAt)} />
                    <SameSpaceRow
                        label="반납일시"
                        value={isBorrowRequest ? `${dateFormatter(returnAt)} (예정)` : dateFormatter(returnAt)}
                    />
                </div>
                <div className="text-center">
                    <p className="bold-16px">{`위와 같이 ${isBorrowRequest ? "물품대여" : "물품반납"}을 신청합니다.`}</p>
                    <p className="regular-14px text-placeholder mt-1">{dateFormatter(requestAt)}</p>
                </div>
                {state != REQUEST_STATE_TYPE.ASSIGNED ? (
                    <p className="text-center border-t border-black mt-2 pt-2">
                        {REQUEST_STATE_TYPE.PERMIT ? "허가" : "불허가"}
                        처리함({dateFormatter(current.response!.createdAt)})
                    </p>
                ) : null}
            </div>

            {state != REQUEST_STATE_TYPE.ASSIGNED ? (
                <Button
                    title={isPermitedRequest ? "불허가로 변경" : "허가로 변경"}
                    className="mt-3 w-full py-2 bold-16px"
                    style={{
                        backgroundColor: isPermitedRequest ? "#ff3b30" : "black",
                    }}
                    onClick={isPermitedRequest ? notAllowBtnOnclick : allowBtnOnclick}
                />
            ) : (
                <div className="flex gap-1 mt-3">
                    <Button title="불허가" className="flex-1 py-2 bold-16px bg-alert!" onClick={notAllowBtnOnclick} />
                    <Button title="허가" className="flex-1 py-2 bold-16px" onClick={allowBtnOnclick} />
                </div>
            )}
            <ConfirmModal
                open={confirmModal}
                title="알림"
                message={confirmModalContent}
                onClose={() => setConfirmModal(false)}
                onConfirm={onConfirm}
            />
            <PromptModal
                open={promptModal}
                title="사유입력"
                placeholder="불허가 사유를 입력해주세요."
                onClose={() => setPromptModal(false)}
                onConfirm={(v: string) => onPromptSubmit(v)}
            />
            <Loading open={loading} />
        </>
    );
}
