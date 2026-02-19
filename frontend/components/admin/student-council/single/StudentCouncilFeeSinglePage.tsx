"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import PromptModal from "@/components/utilities/modal/PromptModal";
import AdminStudentCouncilFeeContext from "@/context/AdminStudentCouncilFeeContext";
import { dateFormatter } from "@/utilities/dateFormatter";
import errorHandler from "@/utilities/errorHandler";
import axios, { AxiosError } from "axios";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function StudentCouncilFeeSinglePage({ id }: { id: number }) {
    const router = useRouter();

    const [confirmModal, setConfirmModal] = useState(false);
    const [confirmModalMsg, setConfirmModalMsg] = useState("");
    const [confirmModalFunc, setConfirmModalFunc] = useState<() => void>(() => {});

    const [promptModal, setPromptModal] = useState(false);
    const [promptModalMsg, setPromptModalMsg] = useState("");
    const [promptModalFunc, setPromptModalFunc] = useState<(v: string) => void>(() => {});

    const studentCouncilFeeList = useContext(AdminStudentCouncilFeeContext).studentCouncilFeeList;
    const setStudentCouncilFeeList = useContext(AdminStudentCouncilFeeContext).setStudentCouncilFeeList;
    const selected = studentCouncilFeeList.find((fee) => fee.id == id);

    if (!selected) {
        notFound();
    }

    const permit = async () => {
        try {
            await axios.patch(`${API_SERVER}/student-council-fee-verification/${selected.id}/permit`, null, {
                withCredentials: true,
            });

            if (setStudentCouncilFeeList) {
                // 변경사항 업데이트
                setStudentCouncilFeeList(
                    studentCouncilFeeList.map((f) => {
                        if (f.id == selected.id) {
                            return {
                                ...selected,
                                verify: true,
                                responseAt: dateFormatter(new Date()),
                            };
                        }
                        return f;
                    }),
                );
            }

            alert("승인 완료했습니다.");

            router.back();
        } catch (error) {
            console.log(error);
            if (error instanceof AxiosError) errorHandler(error);
        }
    };
    const deny = async (denyReason: string) => {
        try {
            await axios.patch(
                `${API_SERVER}/student-council-fee-verification/${selected.id}/deny`,
                { denyReason },
                { withCredentials: true },
            );

            if (setStudentCouncilFeeList) {
                // 변경사항 업데이트
                setStudentCouncilFeeList(
                    studentCouncilFeeList.map((f) => {
                        if (f.id == selected.id) {
                            return {
                                ...selected,
                                verify: false,
                                denyReason,
                                responseAt: dateFormatter(new Date()),
                            };
                        }
                        return f;
                    }),
                );
            }

            alert("미승인 완료했습니다.");

            router.back();
        } catch (error) {
            console.log(error);
            if (error instanceof AxiosError) errorHandler(error);
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px mb-2">등록금 인증사진</p>
            <div
                className="w-full h-60 bg-no-repeat bg-center bg-contain rounded-xl border border-boxBorder bg-white cursor-pointer"
                style={{
                    backgroundImage: `url(${selected.s3Link})`,
                }}
                onClick={() => window.open(selected.s3Link!)}
            />
            <div className="mt-3 flex flex-col">
                <InfoRow label="제출자" value={`${selected.borrowerName} (${selected.borrowerId})}`} />
                <InfoRow label="제출일시" value={dateFormatter(selected.requestAt!)} />
                <InfoRow label="상태" value={`${selected.verify! ? "승인" : "미승인"}`} />
                {selected.verify ? null : (
                    <InfoRow
                        label="미승인사유"
                        value={`${selected.responseAt ? selected.denyReason : "심사이전상태"}`}
                    />
                )}
            </div>
            <div className="mt-2 flex gap-1">
                {selected.responseAt ? (
                    <Button
                        title={selected.verify ? "미승인으로 변경하기" : "승인으로 변경하기"}
                        className="w-full py-2 bold-18px"
                        onClick={() => {
                            if (selected.verify) {
                                setPromptModal(true);
                                setPromptModalMsg("미승인으로 변경하시는 사유를 입력해주세요.");
                                setPromptModalFunc(() => (value: string) => deny(value));
                            } else {
                                setConfirmModal(true);
                                setConfirmModalMsg("승인으로 변경하시겠습니까?");
                                setConfirmModalFunc(() => () => permit());
                            }
                        }}
                    />
                ) : (
                    <>
                        <Button
                            title="미승인하기"
                            className="flex-1 py-2 bold-18px bg-white! border-2 border-black! text-black!"
                            onClick={() => {
                                setPromptModal(true);
                                setPromptModalMsg("미승인 사유를 입력해주세요.");
                                setPromptModalFunc(() => (value: string) => deny(value));
                            }}
                        />
                        <Button
                            title="승인하기"
                            className="flex-1 py-3 bold-18px"
                            onClick={() => {
                                setConfirmModal(true);
                                setConfirmModalMsg("승인하시겠습니까?");
                                setConfirmModalFunc(() => () => permit());
                            }}
                        />
                    </>
                )}
            </div>
            <ConfirmModal
                open={confirmModal}
                title="알림"
                message={confirmModalMsg}
                onConfirm={confirmModalFunc}
                onClose={() => setConfirmModal(false)}
            />
            <PromptModal
                onClose={() => setPromptModal(false)}
                onConfirm={promptModalFunc}
                open={promptModal}
                placeholder={promptModalMsg}
                title="미승인 사유 입력"
            />
        </div>
    );
}

function InfoRow({ label, value }: { label: string; value: string }) {
    return (
        <div className="flex">
            <div className="flex gap-1 w-22 justify-between">
                {Array.from(label).map((s, key) => (
                    <p key={key}>{s}</p>
                ))}
            </div>
            <p> : {value}</p>
        </div>
    );
}
