"use client";

import * as React from "react";

export type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
    error?: boolean;
};

const Input = React.forwardRef<HTMLInputElement, InputProps>(function Input({ className = "", type, ...props }, ref) {
    return (
        <input
            ref={ref}
            type={type}
            className={[
                "w-full rounded-md border bg-white",
                "px-3 py-3 regular-16px",
                "outline-none",
                "border-boxBorder",
                "placeholder:text-placeholder",
                "text-slate-900",
                "focus:border-black ",
                "disabled:text-placeholder",
                className,
            ].join(" ")}
            {...props}
        />
    );
});

export default Input;
