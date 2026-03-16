import { motion } from 'motion/react';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveAltIcon from '@mui/icons-material/SaveAlt';
import RefreshIcon from '@mui/icons-material/Refresh';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import FaceIcon from '@mui/icons-material/Face';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ShareIcon from '@mui/icons-material/Share';
import BarChartIcon from '@mui/icons-material/BarChart';

const FACE_IMAGE =
  'https://images.unsplash.com/photo-1758600434324-41712d1f530e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjbG9zZSUyMHVwJTIwaHVtYW4lMjBmYWNlJTIwcG9ydHJhaXQlMjBuZXV0cmFsfGVufDF8fHx8MTc3MzM3NzY3M3ww&ixlib=rb-4.1.0&q=80&w=1080';

interface ResultScreenProps {
  selectedFace: number | null;
  selectedEye: 'left' | 'right' | null;
  onStartNew: () => void;
  onBack: () => void;
}

const R = "'Roboto', sans-serif";

// Eye box positions for left vs right
const EYE_POSITIONS = {
  left:  { top: '36%', left: '12%', width: '27%', height: '16%' },
  right: { top: '36%', left: '61%', width: '27%', height: '16%' },
};

export function ResultScreen({
  selectedFace,
  selectedEye,
  onStartNew,
  onBack,
}: ResultScreenProps) {
  const confidence = 94.7;
  const eyePos =
    selectedEye === 'left' ? EYE_POSITIONS.left : EYE_POSITIONS.right;
  const eyeLabel = selectedEye === 'left' ? 'Left Eye' : 'Right Eye';

  const infoRows = [
    {
      icon: <FaceIcon style={{ fontSize: '18px', color: '#F4A259' }} />,
      label: 'Face ID',
      value: `Face ${selectedFace ?? 1}`,
    },
    {
      icon: <VisibilityIcon style={{ fontSize: '18px', color: '#F4A259' }} />,
      label: 'Selected Eye',
      value: eyeLabel,
    },
    {
      icon: <BarChartIcon style={{ fontSize: '18px', color: '#F4A259' }} />,
      label: 'Confidence',
      value: `${confidence}%`,
    },
  ];

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
          Detection Result
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
          <ShareIcon style={{ color: '#9AA0A6', fontSize: '22px' }} />
        </button>
      </div>

      {/* Scrollable content */}
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '16px 20px 20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '14px',
        }}
      >
        {/* Success badge */}
        <motion.div
          initial={{ opacity: 0, y: -6 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            background: 'rgba(52,199,89,0.1)',
            padding: '10px 16px',
            borderRadius: '12px',
            border: '1px solid rgba(52,199,89,0.2)',
            flexShrink: 0,
          }}
        >
          <CheckCircleOutlineIcon style={{ color: '#34C759', fontSize: '20px' }} />
          <span
            style={{
              color: '#34C759',
              fontSize: '13px',
              fontFamily: R,
              fontWeight: 500,
            }}
          >
            Detection successful · YOLOv8n
          </span>
        </motion.div>

        {/* Preview image with bounding box */}
        <motion.div
          initial={{ opacity: 0, scale: 0.97 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.1, duration: 0.4 }}
          style={{
            width: '100%',
            aspectRatio: '4 / 3',
            position: 'relative',
            borderRadius: '16px',
            overflow: 'hidden',
            background: '#0a0a0a',
            flexShrink: 0,
          }}
        >
          <img
            src={FACE_IMAGE}
            alt="Detection result"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              objectPosition: 'center top',
              display: 'block',
            }}
          />

          {/* Highlight overlay */}
          <div
            style={{
              position: 'absolute',
              inset: 0,
              background: 'rgba(0,0,0,0.15)',
              pointerEvents: 'none',
            }}
          />

          {/* Detected eye bounding box */}
          <div
            style={{
              position: 'absolute',
              top: eyePos.top,
              left: eyePos.left,
              width: eyePos.width,
              height: eyePos.height,
              border: '2px solid #F4A259',
              borderRadius: '8px',
              boxShadow:
                '0 0 0 3px rgba(244,162,89,0.25), 0 0 20px rgba(244,162,89,0.4)',
            }}
          >
            {/* Label tag */}
            <div
              style={{
                position: 'absolute',
                bottom: '-26px',
                left: '50%',
                transform: 'translateX(-50%)',
                background: '#F4A259',
                padding: '2px 10px',
                borderRadius: '6px',
                whiteSpace: 'nowrap',
              }}
            >
              <span
                style={{
                  color: '#0F1115',
                  fontSize: '10px',
                  fontWeight: 700,
                  fontFamily: R,
                  letterSpacing: '0.06em',
                }}
              >
                {eyeLabel.toUpperCase()}
              </span>
            </div>
          </div>
        </motion.div>

        {/* Info Card */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, duration: 0.4 }}
          style={{
            background: '#242833',
            borderRadius: '16px',
            padding: '16px 18px',
            display: 'flex',
            flexDirection: 'column',
            gap: '12px',
            border: '1px solid rgba(255,255,255,0.05)',
            flexShrink: 0,
          }}
        >
          <span
            style={{
              color: '#9AA0A6',
              fontSize: '11px',
              fontFamily: R,
              textTransform: 'uppercase',
              letterSpacing: '0.1em',
            }}
          >
            Detection Details
          </span>

          {infoRows.map((row, idx) => (
            <div
              key={idx}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
              }}
            >
              <div
                style={{
                  width: '38px',
                  height: '38px',
                  background: 'rgba(244,162,89,0.1)',
                  borderRadius: '10px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                {row.icon}
              </div>
              <div style={{ flex: 1 }}>
                <div
                  style={{
                    color: '#9AA0A6',
                    fontSize: '12px',
                    fontFamily: R,
                    marginBottom: '2px',
                  }}
                >
                  {row.label}
                </div>
                <div
                  style={{
                    color: '#E6E6E6',
                    fontSize: '15px',
                    fontWeight: 500,
                    fontFamily: R,
                  }}
                >
                  {row.value}
                </div>
              </div>
            </div>
          ))}

          {/* Confidence bar */}
          <div style={{ paddingTop: '4px' }}>
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                marginBottom: '6px',
              }}
            >
              <span
                style={{
                  color: '#9AA0A6',
                  fontSize: '11px',
                  fontFamily: R,
                }}
              >
                Confidence Score
              </span>
              <span
                style={{
                  color: '#F4A259',
                  fontSize: '11px',
                  fontWeight: 600,
                  fontFamily: R,
                }}
              >
                {confidence}%
              </span>
            </div>
            <div
              style={{
                height: '6px',
                background: '#1C1F26',
                borderRadius: '3px',
                overflow: 'hidden',
              }}
            >
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${confidence}%` }}
                transition={{ delay: 0.5, duration: 0.7, ease: 'easeOut' }}
                style={{
                  height: '100%',
                  background:
                    'linear-gradient(90deg, #F4A259 0%, #FFB86C 100%)',
                  borderRadius: '3px',
                }}
              />
            </div>
          </div>
        </motion.div>

        {/* Action Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3, duration: 0.4 }}
          style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '10px',
            flexShrink: 0,
          }}
        >
          <button
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
              gap: '8px',
              boxShadow: '0 4px 16px rgba(244,162,89,0.28)',
            }}
          >
            <SaveAltIcon style={{ fontSize: '20px' }} />
            Save Result
          </button>

          <button
            onClick={onStartNew}
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
              gap: '8px',
            }}
          >
            <RefreshIcon style={{ fontSize: '20px', color: '#F4A259' }} />
            Start New Scan
          </button>

          <button
            onClick={onBack}
            style={{
              width: '100%',
              height: '52px',
              background: '#1C1F26',
              border: '1px solid rgba(255,255,255,0.06)',
              borderRadius: '16px',
              color: '#9AA0A6',
              fontSize: '14px',
              fontWeight: 400,
              fontFamily: R,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
            }}
          >
            <ArrowBackIcon style={{ fontSize: '20px' }} />
            Back
          </button>
        </motion.div>
      </div>
    </div>
  );
}
