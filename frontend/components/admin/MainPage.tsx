"use client";

import AdminRequestContext from "@/context/AdminRequestContext";
import AdminStudentCouncilFeeContext from "@/context/AdminStudentCouncilFeeContext";
import ItemContext from "@/context/ItemContext";
import { ITEM_STATE_TYPE } from "@/types/ItemStateType";
import { REQUEST_TYPE } from "@/types/RequestInterface";
import { useContext } from "react";
import SimpleTable from "./main/SimpleTable";
import Button from "../utilities/Button";
import { useRouter } from "next/navigation";

interface TableContent {
    borrowedCount: number;
    affordCount: number;
    totalCount: number;
}

export default function MainPage() {
    const requestContext = useContext(AdminRequestContext);
    const studentCouncilFeeContext = useContext(AdminStudentCouncilFeeContext);
    const itemContext = useContext(ItemContext);
    const router = useRouter();

    const requestList = requestContext.requestList.map((request) => request.type == REQUEST_TYPE.BORROW);
    const returnRequestList = requestContext.requestList.map((request) => request.type == REQUEST_TYPE.RETURN);
    const notVerifiedStudentCouncilFeeList = studentCouncilFeeContext.studentCouncilFeeList.filter(
        (ele) => !ele.verify,
    );
    const itemList = itemContext.itemList;

    const itemNameList = Array.from(new Set((itemList ?? []).map((it) => it.name)));
    const tableContent: Record<string, TableContent> = {};

    itemList.forEach((item) => {
        if (!tableContent[item.name]) {
            tableContent[item.name] = {
                borrowedCount: 0,
                affordCount: 0,
                totalCount: 0,
            };
        }

        tableContent[item.name].totalCount += 1;

        if (item.state === ITEM_STATE_TYPE.BORROWED) {
            tableContent[item.name].borrowedCount += 1;
        } else {
            tableContent[item.name].affordCount += 1;
        }
    });

    return (
        <div className="mt-5">
            <div>
                <p className="black-20px mb-2">✅ 대여현황</p>
                <SimpleTable
                    headers={["구분", "현재 신청수"]}
                    rows={[
                        ["대여", requestList.length],
                        ["반납", returnRequestList.length],
                    ]}
                />
            </div>
            <div className="mt-5 mb-6">
                <p className="black-20px mb-2">📦 물품현황</p>
                <SimpleTable
                    headers={["물품명", "대여중", "재고", "총합"]}
                    rows={[
                        ...itemNameList.map((name) => {
                            const bCount = tableContent[name].borrowedCount;
                            const aCount = tableContent[name].affordCount;
                            const tCount = bCount + aCount;

                            return [name, bCount, aCount, tCount];
                        }),
                    ]}
                />
            </div>
            <Button
                title={`등록금 납부확인 처리(${notVerifiedStudentCouncilFeeList.length}명 대기중)`}
                className="py-3 w-full bold-18px"
                onClick={() => router.push("/admin/student-council-fee")}
            />
        </div>
    );
}
