import { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import VisibilityIcon from '@mui/icons-material/Visibility';
import CircularProgress from '@mui/material/CircularProgress';

interface SplashScreenProps {
  onComplete: () => void;
}

export function SplashScreen({ onComplete }: SplashScreenProps) {
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(timer);
          setTimeout(onComplete, 400);
          return 100;
        }
        return Math.min(prev + 3.5, 100);
      });
    }, 100);
    return () => clearInterval(timer);
  }, [onComplete]);

  const steps = [
    'Loading model weights…',
    'Initializing AI Detection Engine',
    'Calibrating eye tracking…',
    'Ready',
  ];
  const stepIndex = Math.min(Math.floor(progress / 25), 3);

  return (
    <div
      style={{
        flex: 1,
        background: '#0F1115',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '32px 24px',
        gap: '0px',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Ambient glow */}
      <div
        style={{
          position: 'absolute',
          top: '30%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: '280px',
          height: '280px',
          background: 'radial-gradient(circle, rgba(244,162,89,0.12) 0%, transparent 70%)',
          borderRadius: '50%',
          pointerEvents: 'none',
        }}
      />

      {/* Logo icon */}
      <motion.div
        initial={{ scale: 0.4, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.65, ease: [0.34, 1.56, 0.64, 1] }}
        style={{
          width: '100px',
          height: '100px',
          background: 'linear-gradient(145deg, #F4A259 0%, #FFB86C 100%)',
          borderRadius: '30px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow:
            '0 12px 40px rgba(244,162,89,0.45), 0 4px 12px rgba(0,0,0,0.5)',
          marginBottom: '28px',
        }}
      >
        <VisibilityIcon style={{ color: '#0F1115', fontSize: '54px' }} />
      </motion.div>

      {/* App name */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3, duration: 0.5 }}
        style={{ textAlign: 'center', marginBottom: '8px' }}
      >
        <div
          style={{
            color: '#E6E6E6',
            fontSize: '32px',
            fontWeight: 700,
            fontFamily: "'Roboto', sans-serif",
            letterSpacing: '-0.03em',
            lineHeight: 1.1,
          }}
        >
          ChakraVue
        </div>
        <div
          style={{
            color: '#F4A259',
            fontSize: '14px',
            fontWeight: 500,
            fontFamily: "'Roboto', sans-serif",
            letterSpacing: '0.22em',
            textTransform: 'uppercase',
            marginTop: '6px',
          }}
        >
          Eye Scanner
        </div>
      </motion.div>

      {/* Tagline */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.55, duration: 0.5 }}
        style={{
          color: '#9AA0A6',
          fontSize: '13px',
          fontFamily: "'Roboto', sans-serif",
          textAlign: 'center',
          marginBottom: '48px',
          lineHeight: 1.4,
        }}
      >
        AI-Powered Ocular Detection
      </motion.div>

      {/* Progress ring */}
      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.75, duration: 0.4 }}
        style={{
          position: 'relative',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '20px',
        }}
      >
        {/* Background ring */}
        <CircularProgress
          variant="determinate"
          value={100}
          size={64}
          thickness={2.5}
          style={{ color: '#1C1F26', position: 'absolute' }}
        />
        {/* Progress ring */}
        <CircularProgress
          variant="determinate"
          value={progress}
          size={64}
          thickness={2.5}
          style={{ color: '#F4A259' }}
        />
        <span
          style={{
            position: 'absolute',
            color: '#E6E6E6',
            fontSize: '13px',
            fontWeight: 600,
            fontFamily: "'Roboto', sans-serif",
          }}
        >
          {Math.round(progress)}%
        </span>
      </motion.div>

      {/* Status text */}
      <motion.div
        key={stepIndex}
        initial={{ opacity: 0, y: 6 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        style={{
          color: '#9AA0A6',
          fontSize: '13px',
          fontFamily: "'Roboto', sans-serif",
          textAlign: 'center',
        }}
      >
        {steps[stepIndex]}
      </motion.div>

      {/* Version */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.1, duration: 0.5 }}
        style={{
          position: 'absolute',
          bottom: '24px',
          color: '#3a3f48',
          fontSize: '11px',
          fontFamily: "'Roboto', sans-serif",
          letterSpacing: '0.04em',
        }}
      >
        v2.1.0 · YOLOv8n Engine
      </motion.div>
    </div>
  );
}
