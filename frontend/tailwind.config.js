/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#2E7D32",
          dark:    "#1B5E20",
          light:   "#4CAF50",
        },
        navbar: {
          DEFAULT: "#2C3E50",
          dark:    "#1A252F",
        },
        surface: "#FFFFFF",
        danger:  "#C62828",
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui"],
      },
    },
  },
  plugins: [],
};
