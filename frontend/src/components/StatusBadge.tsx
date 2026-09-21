import React from 'react';
import { Clock, CheckCircle, Package, Truck, CheckCheck, XCircle } from 'lucide-react';
import { OrderStatus } from '../types';

interface StatusBadgeProps {
  status: OrderStatus;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const getIcon = () => {
    switch (status) {
      case 'PENDING':
        return <Clock size={12} />;
      case 'CONFIRMED':
        return <CheckCircle size={12} />;
      case 'PROCESSING':
        return <Package size={12} />;
      case 'SHIPPED':
        return <Truck size={12} />;
      case 'DELIVERED':
        return <CheckCheck size={12} />;
      case 'CANCELLED':
        return <XCircle size={12} />;
      default:
        return null;
    }
  };

  return (
    <span className={`status-badge status-${status}`}>
      {getIcon()}
      {status}
    </span>
  );
};
