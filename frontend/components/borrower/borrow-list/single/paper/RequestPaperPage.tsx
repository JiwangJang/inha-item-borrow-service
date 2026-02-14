"use client";

import LoginRequired from "@/components/borrower/LoginRequired";
import BorrowerContext from "@/context/BorrowerContext";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import { dateFormatter } from "@/utilities/dateFormatter";
import { notFound } from "next/navigation";
import { useContext } from "react";

export default function RequestPaperPage({ id }: { id: string }) {
    const requestList = useContext(BorrowRequestContext).requestList;
    const borrowerInfo = useContext(BorrowerContext).borrowerInfo;
    const current = requestList.find((r) => String(r.id) == id);

    if (current == null) {
        notFound();
    }

    if (borrowerInfo == null) {
        return <LoginRequired />;
    }

    const { name: stName, phoneNumber, id: studentNumber } = borrowerInfo;
    const { borrowAt, returnAt, createdAt } = current;
    const { name: itemName } = current.item;
    const response = current.response;
    const responseAt = response?.createdAt;

    return (
        <div className="bg-white border border-boxBorder rounded-xl mt-5 py-5 px-6">
            <p className="black-24px text-center">물품대여신청서</p>
            <div className="mt-5 regular-16px flex flex-col gap-1">
                <PaperRow label="이름" value={stName} />
                <PaperRow label="연락처" value={phoneNumber} />
                <PaperRow label="학번" value={studentNumber} />
                <PaperRow label="대여물품" value={itemName} />
                <PaperRow label="대여일시" value={dateFormatter(borrowAt)} />
                <PaperRow label="반납일시" value={`${dateFormatter(returnAt)}(예정)`} />
            </div>

            <div className="mt-4 text-center">
                <p className="bold-16px">본인은 위와 같이 물품대여신청합니다.</p>
                <p className="regular-16px">{dateFormatter(createdAt).slice(0, 13)}</p>
            </div>

            {response != null ? (
                <div className="mt-4 pt-4 border-t border-black text-center bold-16px flex flex-col justify-center items-center">
                    <p>위 사람의 물품대여신청을 허가합니다.</p>
                    <div className="my-5 relative w-fit">
                        <p className="black-20px">미래융합대학 학생회장</p>
                        <div
                            className="-top-2 -right-3 absolute w-10 h-10"
                            style={{
                                backgroundImage: "url(/images/stamp.png)",
                                backgroundSize: "cover",
                                backgroundRepeat: "no-repeat",
                            }}
                        ></div>
                    </div>

                    <p>{dateFormatter(responseAt!).slice(0, 13)}</p>
                </div>
            ) : null}
        </div>
    );
}

function PaperRow({ label, value }: { label: string; value: React.ReactNode }) {
    function splitCharacters(text: string): string[] {
        // Array.from handles Unicode properly (including Korean characters)
        return Array.from(text);
    }

    return (
        <div className="flex">
            {/* Stretch the label text so the colon aligns nicely (Korean-friendly) */}
            <div className="w-15 flex justify-between">
                {splitCharacters(label).map((s, i) => (
                    <span key={i}>{s}</span>
                ))}
            </div>
            <span className="mx-1">:</span>
            <span>{value}</span>
        </div>
    );
}
