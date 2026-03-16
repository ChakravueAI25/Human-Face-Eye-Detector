import { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import FlashAutoIcon from '@mui/icons-material/FlashAuto';
import FlipCameraAndroidIcon from '@mui/icons-material/FlipCameraAndroid';
import GridOnIcon from '@mui/icons-material/GridOn';

interface CameraScreenProps {
  onCapture: () => void;
  onBack: () => void;
}

const R = "'Roboto', sans-serif";

const CORNERS = [
  { key: 'tl', style: { top: 0, left: 0, borderTopWidth: 3, borderLeftWidth: 3, borderTopLeftRadius: 6 } },
  { key: 'tr', style: { top: 0, right: 0, borderTopWidth: 3, borderRightWidth: 3, borderTopRightRadius: 6 } },
  { key: 'bl', style: { bottom: 0, left: 0, borderBottomWidth: 3, borderLeftWidth: 3, borderBottomLeftRadius: 6 } },
  { key: 'br', style: { bottom: 0, right: 0, borderBottomWidth: 3, borderRightWidth: 3, borderBottomRightRadius: 6 } },
] as const;

export function CameraScreen({ onCapture, onBack }: CameraScreenProps) {
  const [capturing, setCapturing] = useState(false);
  const [flash, setFlash] = useState(false);

  const handleCapture = () => {
    if (capturing) return;
    setCapturing(true);
    setFlash(true);
    setTimeout(() => setFlash(false), 250);
    setTimeout(() => {
      setCapturing(false);
      onCapture();
    }, 900);
  };

  return (
    <div
      style={{
        flex: 1,
        background: '#0F1115',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      {/* Top App Bar */}
      <div
        style={{
          height: '56px',
          background: '#1C1F26',
          display: 'flex',
          alignItems: 'center',
          paddingLeft: '4px',
          paddingRight: '8px',
          flexShrink: 0,
          boxShadow: '0 1px 0 rgba(255,255,255,0.05)',
        }}
      >
        <button
          onClick={onBack}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '10px',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <ArrowBackIcon style={{ color: '#E6E6E6', fontSize: '24px' }} />
        </button>
        <span
          style={{
            color: '#E6E6E6',
            fontSize: '20px',
            fontWeight: 600,
            fontFamily: R,
            marginLeft: '4px',
            flex: 1,
          }}
        >
          Camera Capture
        </span>
        <button
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '10px',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <FlashAutoIcon style={{ color: '#9AA0A6', fontSize: '22px' }} />
        </button>
        <button
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '10px',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <GridOnIcon style={{ color: '#9AA0A6', fontSize: '20px' }} />
        </button>
      </div>

      {/* Viewfinder */}
      <div
        style={{
          flex: 1,
          position: 'relative',
          background: '#080a0c',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          overflow: 'hidden',
        }}
      >
        {/* Camera texture gradient */}
        <div
          style={{
            position: 'absolute',
            inset: 0,
            background:
              'radial-gradient(ellipse at center, #1a1c20 0%, #0a0b0d 70%)',
          }}
        />

        {/* Grid lines (rule of thirds) */}
        {[33, 66].map((p) => (
          <div key={`h${p}`}
            style={{
              position: 'absolute',
              left: 0, right: 0,
              top: `${p}%`,
              height: '1px',
              background: 'rgba(255,255,255,0.06)',
            }}
          />
        ))}
        {[33, 66].map((p) => (
          <div key={`v${p}`}
            style={{
              position: 'absolute',
              top: 0, bottom: 0,
              left: `${p}%`,
              width: '1px',
              background: 'rgba(255,255,255,0.06)',
            }}
          />
        ))}

        {/* Scan frame */}
        <div
          style={{
            position: 'relative',
            width: '240px',
            height: '300px',
          }}
        >
          {/* Corner brackets */}
          {CORNERS.map(({ key, style }) => (
            <div
              key={key}
              style={{
                position: 'absolute',
                width: '28px',
                height: '28px',
                borderColor: '#F4A259',
                borderStyle: 'solid',
                borderWidth: 0,
                ...style,
              }}
            />
          ))}

          {/* Scanning line */}
          <motion.div
            animate={{ y: [0, 300, 0] }}
            transition={{ duration: 2.8, repeat: Infinity, ease: 'linear' }}
            style={{
              position: 'absolute',
              left: '8px',
              right: '8px',
              height: '2px',
              background:
                'linear-gradient(90deg, transparent, #F4A259 30%, #FFB86C 50%, #F4A259 70%, transparent)',
              boxShadow: '0 0 10px rgba(244,162,89,0.8)',
            }}
          />

          {/* Center crosshair dot */}
          <div
            style={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              width: '6px',
              height: '6px',
              background: '#F4A259',
              borderRadius: '50%',
              boxShadow: '0 0 8px rgba(244,162,89,0.8)',
            }}
          />
        </div>

        {/* Instruction text */}
        <div
          style={{
            position: 'absolute',
            bottom: '20px',
            color: '#9AA0A6',
            fontSize: '13px',
            fontFamily: R,
            textAlign: 'center',
            letterSpacing: '0.02em',
          }}
        >
          Position face within the frame
        </div>

        {/* Flash overlay */}
        <AnimatePresence>
          {flash && (
            <motion.div
              initial={{ opacity: 0.9 }}
              animate={{ opacity: 0 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.25 }}
              style={{
                position: 'absolute',
                inset: 0,
                background: 'white',
                pointerEvents: 'none',
              }}
            />
          )}
        </AnimatePresence>

        {/* Processing overlay */}
        <AnimatePresence>
          {capturing && !flash && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              style={{
                position: 'absolute',
                inset: 0,
                background: 'rgba(0,0,0,0.55)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexDirection: 'column',
                gap: '12px',
              }}
            >
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 0.9, repeat: Infinity, ease: 'linear' }}
                style={{
                  width: '40px',
                  height: '40px',
                  border: '3px solid rgba(244,162,89,0.3)',
                  borderTopColor: '#F4A259',
                  borderRadius: '50%',
                }}
              />
              <span style={{ color: '#E6E6E6', fontSize: '14px', fontFamily: R }}>
                Processing…
              </span>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Bottom Controls */}
      <div
        style={{
          height: '116px',
          background: '#1C1F26',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          paddingLeft: '40px',
          paddingRight: '40px',
          flexShrink: 0,
          borderTop: '1px solid rgba(255,255,255,0.05)',
        }}
      >
        {/* Thumbnail placeholder */}
        <div
          style={{
            width: '52px',
            height: '52px',
            background: '#242833',
            borderRadius: '12px',
            border: '1px solid rgba(255,255,255,0.08)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <span style={{ color: '#3a3f48', fontSize: '10px', fontFamily: R }}>
            Gallery
          </span>
        </div>

        {/* Shutter button */}
        <button
          onClick={handleCapture}
          style={{
            width: '72px',
            height: '72px',
            borderRadius: '50%',
            border: `3px solid ${capturing ? '#cc8844' : '#F4A259'}`,
            background: 'transparent',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 0,
            transition: 'border-color 0.15s',
            flexShrink: 0,
          }}
        >
          <div
            style={{
              width: '58px',
              height: '58px',
              borderRadius: '50%',
              background: capturing
                ? '#cc8844'
                : 'linear-gradient(145deg, #F4A259, #FFB86C)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              transition: 'background 0.15s',
            }}
          >
            <CameraAltIcon style={{ color: '#0F1115', fontSize: '26px' }} />
          </div>
        </button>

        {/* Flip button */}
        <div
          style={{
            width: '52px',
            height: '52px',
            background: '#242833',
            borderRadius: '50%',
            border: '1px solid rgba(255,255,255,0.08)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
          }}
        >
          <FlipCameraAndroidIcon style={{ color: '#9AA0A6', fontSize: '22px' }} />
        </div>
      </div>
    </div>
  );
}
