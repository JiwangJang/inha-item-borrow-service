"use client";

import * as React from "react";

export type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
    error?: boolean;
};

/**
 * Design-matched input component
 * - padding: left/right 12px (px-3), top/bottom 16px (py-4)
 * - type is controlled by parent (e.g. password visibility toggle)
 */
export default function Input({ className = "", type = "text", ...props }: InputProps) {
    return (
        <input
            type={type}
            className={[
                "w-full rounded-md border bg-white",
                "px-3 py-3 regular-16px",
                "outline-none",
                "border-boxBorder",
                "placeholder:text-placeholder",
                "text-slate-900",
                "focus:border-black ",
                className,
            ].join(" ")}
            {...props}
        />
    );
}
