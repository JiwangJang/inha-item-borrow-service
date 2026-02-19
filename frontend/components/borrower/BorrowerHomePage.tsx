"use client";

import { useRouter } from "next/navigation";
import Button from "../utilities/Button";
import ItemSection from "./borrower-home-page/ItemSection";
import NoticeSection from "./borrower-home-page/NoticeSection";
import { useContext } from "react";
import BorrowerContext from "@/context/BorrowerContext";

export default function BorrowerHomePage() {
    const { borrowerInfo } = useContext(BorrowerContext);
    const router = useRouter();

    return (
        <div className="mt-5 relative">
            <NoticeSection />
            <ItemSection />
            <div className="h-16" />
            <div className="fixed w-full max-w-125 bottom-17.5 left-1/2 translate-x-[-50%] pl-6 pr-9">
                <Button
                    title={borrowerInfo?.id == null ? "로그인하기" : "대여신청"}
                    className="w-full py-3 bold-18px"
                    onClick={() => {
                        router.push(borrowerInfo?.id == null ? "/login" : "/borrower-request");
                    }}
                />
            </div>
        </div>
    );
}
