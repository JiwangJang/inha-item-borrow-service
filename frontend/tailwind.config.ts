module.exports = {
    content: [
        "./app/**/*.{js,ts,jsx,tsx,mdx}",
        "./pages/**/*.{js,ts,jsx,tsx,mdx}",
        "./components/**/*.{js,ts,jsx,tsx,mdx}",
        "./src/**/*.{js,ts,jsx,tsx,mdx}",
    ],
    theme: {
        extend: {
            fontFamily: {
                pretendard: ["Pretendard", "system-ui", "sans-serif"],
            },
            colors: {
                boxBorder: "#ececec",
                placeholder: "#a6a6a6",
                back: "#f9f9f9",
                alert: "#ff3b30",
                available: "#99ff87",
                unavailable: "#ffc1bd",
            },
        },
    },
};
