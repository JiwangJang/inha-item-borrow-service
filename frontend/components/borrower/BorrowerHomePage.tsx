"use client";

import { useRouter } from "next/navigation";
import Button from "../utilities/Button";
import ItemSection from "./borrower-home-page/ItemSection";
import NoticeSection from "./borrower-home-page/NoticeSection";

export default function BorrowerHomePage() {
    const router = useRouter();
    return (
        <div className="mt-5 relative">
            <NoticeSection />
            <ItemSection />
            <div className="h-16" />
            <div className="fixed w-full bottom-17.5 right-0 px-6">
                <Button
                    title="대여신청"
                    className="w-full py-3 bold-18px"
                    onClick={() => {
                        router.push("/borrower-request");
                    }}
                />
            </div>
        </div>
    );
}
