import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        // Colores institucionales UPTC según el manual
        'uptc-yellow': {
          DEFAULT: '#FFCC29',
          50: '#FFFBF0',
          100: '#FFF5D6',
          200: '#FFE9AD',
          300: '#FFDD84',
          400: '#FFD45B',
          500: '#FFCC29',
          600: '#E6B824',
          700: '#CCA320',
          800: '#B38F1C',
          900: '#997A18',
          950: '#806514',
        },
        'uptc-black': {
          DEFAULT: '#1A1A1A',
          50: '#666666',
          100: '#595959',
          200: '#4D4D4D',
          300: '#404040',
          400: '#333333',
          500: '#1A1A1A',
          600: '#171717',
          700: '#141414',
          800: '#0F0F0F',
          900: '#0A0A0A',
          950: '#050505',
        },
      },
      fontFamily: {
        // Tipografía institucional - Usando Georgia como alternativa web-safe a Dutch810
        sans: ['Georgia', 'Times New Roman', 'serif'],
        serif: ['Georgia', 'Times New Roman', 'serif'],
        // Si decides agregar Dutch810 via @font-face, puedes descomentar:
        // 'dutch': ['Dutch810 Rm BT', 'Georgia', 'serif'],
      },
      spacing: {
        // Áreas de seguridad del logo según el manual (múltiplos de 'x')
        'logo-safe': '3rem', // equivalente a 3x en el manual
      },
      boxShadow: {
        'uptc': '0 4px 12px rgba(255, 204, 41, 0.2)',
        'uptc-lg': '0 8px 24px rgba(255, 204, 41, 0.3)',
        'uptc-xl': '0 12px 36px rgba(255, 204, 41, 0.4)',
      },
      animation: {
        'pulse-uptc': 'pulse-uptc 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        'pulse-uptc': {
          '0%, 100%': {
            opacity: '1',
          },
          '50%': {
            opacity: '0.7',
          },
        },
      },
      backgroundImage: {
        'gradient-uptc': 'linear-gradient(135deg, #1A1A1A 0%, #333333 50%, #1A1A1A 100%)',
        'gradient-uptc-yellow': 'linear-gradient(135deg, #FFCC29 0%, #E6B824 100%)',
      },
    },
  },
  plugins: [],
};

export default config;