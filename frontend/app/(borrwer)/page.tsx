"use client";

import Loading from "@/components/utilities/Loading";
import AlertModal from "@/components/utilities/modal/AlertModal";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import PromptModal from "@/components/utilities/modal/PromptModal";
import { useState } from "react";

export default function Page() {
    const [loading, setLoading] = useState(false);

    const fakeWork = async () => {
        setLoading(true);
        try {
            await new Promise((r) => setTimeout(r, 3000));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6">
            <button className="px-4 py-2 rounded bg-black text-white" onClick={fakeWork}>
                Start
            </button>

            <Loading open={loading} />
        </div>
    );
}
