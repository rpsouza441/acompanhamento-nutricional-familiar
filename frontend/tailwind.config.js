/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        forest: {
          50: '#eefbf0',
          100: '#d8f6dc',
          600: '#096430',
          700: '#064f26',
        },
        skycare: {
          50: '#eef5ff',
          600: '#0058be',
        },
        cranberry: '#8e394c',
        ink: '#151c27',
        graphite: '#404940',
        mist: '#f9f9ff',
        line: '#dce2f3',
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 8px 24px rgba(21, 28, 39, 0.07)',
      },
    },
  },
  plugins: [],
};
