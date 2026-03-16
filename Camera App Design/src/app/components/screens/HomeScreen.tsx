import { motion } from 'motion/react';
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import VideocamIcon from '@mui/icons-material/Videocam';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import VisibilityIcon from '@mui/icons-material/Visibility';
import TuneIcon from '@mui/icons-material/Tune';

interface HomeScreenProps {
  onCapture: () => void;
  onUpload: () => void;
  onLiveCamera: () => void;
}

const R = "'Roboto', sans-serif";

export function HomeScreen({ onCapture, onUpload, onLiveCamera }: HomeScreenProps) {
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
          paddingLeft: '20px',
          paddingRight: '16px',
          flexShrink: 0,
          boxShadow: '0 1px 0 rgba(255,255,255,0.05)',
        }}
      >
        <VisibilityIcon style={{ color: '#F4A259', fontSize: '22px', marginRight: '12px' }} />
        <span
          style={{
            color: '#E6E6E6',
            fontSize: '20px',
            fontWeight: 600,
            fontFamily: R,
            flex: 1,
          }}
        >
          Eye Detection
        </span>
        <button
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '8px',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <TuneIcon style={{ color: '#9AA0A6', fontSize: '22px' }} />
        </button>
      </div>

      {/* Scrollable content */}
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '20px 20px 24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px',
        }}
      >
        {/* Hero Card */}
        <motion.div
          initial={{ opacity: 0, y: -8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          style={{
            background: '#1C1F26',
            borderRadius: '20px',
            padding: '24px 20px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '10px',
            border: '1px solid rgba(255,255,255,0.04)',
          }}
        >
          <div
            style={{
              width: '68px',
              height: '68px',
              background: 'rgba(244,162,89,0.14)',
              borderRadius: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '4px',
            }}
          >
            <VisibilityIcon style={{ color: '#F4A259', fontSize: '38px' }} />
          </div>
          <div
            style={{
              color: '#E6E6E6',
              fontSize: '17px',
              fontWeight: 600,
              fontFamily: R,
              textAlign: 'center',
            }}
          >
            AI-Powered Eye Detection
          </div>
          <div
            style={{
              color: '#9AA0A6',
              fontSize: '13px',
              fontFamily: R,
              textAlign: 'center',
              lineHeight: 1.55,
              maxWidth: '260px',
            }}
          >
            Capture or upload an image to detect and analyze eye regions using YOLOv8
          </div>
        </motion.div>

        {/* Section label */}
        <div
          style={{
            color: '#9AA0A6',
            fontSize: '11px',
            fontFamily: R,
            textTransform: 'uppercase',
            letterSpacing: '0.1em',
            paddingLeft: '4px',
            paddingTop: '4px',
          }}
        >
          Select Input Method
        </div>

        {/* Primary Button */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1, duration: 0.4 }}
          style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}
        >
          <button
            onClick={onCapture}
            style={{
              width: '100%',
              height: '52px',
              background: 'linear-gradient(135deg, #F4A259 0%, #FFB86C 100%)',
              border: 'none',
              borderRadius: '16px',
              color: '#0F1115',
              fontSize: '15px',
              fontWeight: 600,
              fontFamily: R,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '10px',
              boxShadow: '0 4px 16px rgba(244,162,89,0.30)',
            }}
          >
            <CameraAltIcon style={{ fontSize: '22px' }} />
            Capture Image
          </button>

          <button
            onClick={onUpload}
            style={{
              width: '100%',
              height: '52px',
              background: '#242833',
              border: '1px solid rgba(255,255,255,0.07)',
              borderRadius: '16px',
              color: '#E6E6E6',
              fontSize: '15px',
              fontWeight: 500,
              fontFamily: R,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '10px',
            }}
          >
            <CloudUploadIcon style={{ fontSize: '22px', color: '#F4A259' }} />
            Upload Image
          </button>

          <button
            onClick={onLiveCamera}
            style={{
              width: '100%',
              height: '52px',
              background: '#242833',
              border: '1px solid rgba(255,255,255,0.07)',
              borderRadius: '16px',
              color: '#E6E6E6',
              fontSize: '15px',
              fontWeight: 500,
              fontFamily: R,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '10px',
            }}
          >
            <VideocamIcon style={{ fontSize: '22px', color: '#F4A259' }} />
            Live Camera Detection
          </button>
        </motion.div>

        {/* Model Info Card */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, duration: 0.4 }}
          style={{
            background: '#242833',
            borderRadius: '16px',
            padding: '16px 18px',
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
            border: '1px solid rgba(255,255,255,0.05)',
          }}
        >
          <div
            style={{
              width: '42px',
              height: '42px',
              background: 'rgba(244,162,89,0.12)',
              borderRadius: '12px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <InfoOutlinedIcon style={{ color: '#F4A259', fontSize: '22px' }} />
          </div>
          <div style={{ flex: 1 }}>
            <div
              style={{
                color: '#9AA0A6',
                fontSize: '11px',
                fontFamily: R,
                textTransform: 'uppercase',
                letterSpacing: '0.08em',
                marginBottom: '6px',
              }}
            >
              Detection Model
            </div>
            <div style={{ display: 'flex', gap: '20px' }}>
              <div>
                <span style={{ color: '#9AA0A6', fontSize: '13px', fontFamily: R }}>
                  Model:{' '}
                </span>
                <span
                  style={{
                    color: '#F4A259',
                    fontSize: '13px',
                    fontWeight: 600,
                    fontFamily: R,
                  }}
                >
                  YOLOv8n
                </span>
              </div>
              <div>
                <span style={{ color: '#9AA0A6', fontSize: '13px', fontFamily: R }}>
                  Input:{' '}
                </span>
                <span
                  style={{
                    color: '#E6E6E6',
                    fontSize: '13px',
                    fontWeight: 500,
                    fontFamily: R,
                  }}
                >
                  640×640
                </span>
              </div>
            </div>
          </div>
          {/* Status dot */}
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px', flexShrink: 0 }}>
            <div
              style={{
                width: '8px',
                height: '8px',
                borderRadius: '50%',
                background: '#34C759',
                boxShadow: '0 0 6px rgba(52,199,89,0.7)',
              }}
            />
            <span style={{ color: '#34C759', fontSize: '10px', fontFamily: R }}>Ready</span>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
