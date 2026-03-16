import { ReactNode } from 'react';
import SignalCellularAltIcon from '@mui/icons-material/SignalCellularAlt';
import WifiIcon from '@mui/icons-material/Wifi';
import BatteryFullIcon from '@mui/icons-material/BatteryFull';

interface PhoneFrameProps {
  children: ReactNode;
}

export function PhoneFrame({ children }: PhoneFrameProps) {
  const now = new Date();
  const time = now.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });

  return (
    <div
      style={{
        position: 'relative',
        width: '375px',
        background: '#0A0B0E',
        borderRadius: '50px',
        boxShadow:
          '0 0 0 2px #2a2a2a, 0 0 0 5px #111, 0 50px 120px rgba(0,0,0,0.9)',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        userSelect: 'none',
      }}
    >
      {/* Punch-hole camera */}
      <div
        style={{
          position: 'absolute',
          top: '16px',
          left: '50%',
          transform: 'translateX(-50%)',
          width: '13px',
          height: '13px',
          background: '#080a0c',
          borderRadius: '50%',
          zIndex: 200,
          boxShadow: 'inset 0 0 4px rgba(0,0,0,0.8), 0 0 0 1px #1a1a1a',
        }}
      />

      {/* Status Bar */}
      <div
        style={{
          height: '44px',
          background: '#0F1115',
          display: 'flex',
          alignItems: 'flex-end',
          paddingBottom: '6px',
          paddingLeft: '24px',
          paddingRight: '20px',
          flexShrink: 0,
          zIndex: 100,
        }}
      >
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            width: '100%',
            alignItems: 'center',
          }}
        >
          <span
            style={{
              color: '#E6E6E6',
              fontSize: '13px',
              fontWeight: 600,
              fontFamily: "'Roboto', sans-serif",
              letterSpacing: '0.02em',
              lineHeight: 1,
            }}
          >
            {time}
          </span>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
            }}
          >
            <SignalCellularAltIcon
              style={{ color: '#E6E6E6', fontSize: '15px' }}
            />
            <WifiIcon style={{ color: '#E6E6E6', fontSize: '15px' }} />
            <BatteryFullIcon style={{ color: '#E6E6E6', fontSize: '17px' }} />
          </div>
        </div>
      </div>

      {/* Screen Content */}
      <div
        style={{
          height: '756px',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          background: '#0F1115',
          position: 'relative',
        }}
      >
        {children}
      </div>

      {/* Home Indicator */}
      <div
        style={{
          height: '24px',
          background: '#0F1115',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
        }}
      >
        <div
          style={{
            width: '128px',
            height: '5px',
            background: '#3a3a3a',
            borderRadius: '3px',
          }}
        />
      </div>
    </div>
  );
}
