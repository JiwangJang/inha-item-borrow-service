"use client";

import Spinner from "./Spinner";

export default function Loading({ open }: { open: boolean }) {
    if (!open) return null;

    return (
        <div
            className={["fixed inset-0 z-50", "pointer-events-none"].join(" ")}
            role="status"
            aria-live="polite"
            aria-busy="true"
        >
            {/* Backdrop */}
            <div className="absolute inset-0 bg-black/35" />

            {/* Center content */}
            <div className="absolute inset-0 flex flex-col items-center justify-center gap-4">
                <Spinner size={88} thickness={8} />
                <div className="bold-20px text-white">작업 진행중</div>
            </div>
        </div>
    );
}
