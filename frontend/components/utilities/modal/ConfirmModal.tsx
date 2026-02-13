"use client";

import BaseModal from "./BaseModal";

export type ConfirmModalProps = {
    open: boolean;
    onClose: () => void;
    title?: string;
    message: React.ReactNode;

    confirmText?: string;
    cancelText?: string;

    onConfirm: () => void;
    onCancel?: () => void;
};

export default function ConfirmModal({ open, onClose, title, message, onConfirm, onCancel }: ConfirmModalProps) {
    return (
        <BaseModal open={open} onClose={onClose} title={title}>
            <div className="regular-16px pb-2">{message}</div>

            <div className="h-px bg-boxBorder" />

            <div className="flex">
                <button
                    type="button"
                    className="py-2.5 text-placeholder regular-16px hover:bg-slate-100 flex-1 cursor-pointer"
                    onClick={() => {
                        onCancel?.();
                        onClose();
                    }}
                >
                    취소
                </button>

                <div className="w-px bg-boxBorder" />

                <button
                    type="button"
                    className="py-2.5 regular-16px  hover:bg-slate-100 flex-1 cursor-pointer"
                    onClick={() => {
                        onConfirm();
                        onClose();
                    }}
                >
                    확인
                </button>
            </div>
        </BaseModal>
    );
}
