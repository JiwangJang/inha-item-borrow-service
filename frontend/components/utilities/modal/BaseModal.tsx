"use client";

import { useEffect, useState } from "react";
import { createPortal } from "react-dom";

export type BaseModalProps = {
    open: boolean;
    onClose: () => void;

    title?: string;
    children: React.ReactNode;

    closeOnBackdrop?: boolean;
    closeOnEsc?: boolean;

    /** 트랜지션 시간(ms) - CSS duration과 맞춰야 함 */
    transitionMs?: number;
};

export default function BaseModal({ open, onClose, title, children, transitionMs = 200 }: BaseModalProps) {
    const [mounted, setMounted] = useState(false);
    const [render, setRender] = useState(open);

    useEffect(() => setMounted(true), []);

    useEffect(() => {
        // 모달 생겼을때 애니메이션
        if (open) {
            setRender(true);
            document.body.style.overflow = "hidden";
        } else {
            const t = setTimeout(() => setRender(false), transitionMs);
            document.body.style.overflow = "";
            return () => clearTimeout(t);
        }
    }, [open, transitionMs]);

    useEffect(() => {
        if (!open) return;
    }, [open, onClose]);

    if (!mounted || !render) return null;

    return createPortal(
        <div className="fixed inset-0 z-50">
            {/* Backdrop */}
            <div
                className={[
                    "absolute inset-0 bg-black/40 transition-opacity duration-200",
                    open ? "opacity-100" : "opacity-0",
                ].join(" ")}
            />

            {/* Dialog */}
            <div className="w-full absolute inset-0 flex items-center justify-center">
                <div
                    role="dialog"
                    aria-modal="true"
                    className={[
                        "w-58.5 overflow-hidden rounded-2xl bg-white shadow-xl",
                        "transition-all duration-200",
                        open ? "opacity-100 translate-y-0 scale-100" : "opacity-0 translate-y-2 scale-95",
                    ].join(" ")}
                    onClick={(e) => e.stopPropagation()}
                >
                    <div className="text-center">
                        <div className="bold-18px pt-3">{title}</div>
                        <div>{children}</div>
                    </div>
                </div>
            </div>
        </div>,
        document.body,
    );
}
