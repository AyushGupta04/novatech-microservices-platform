import React, { useState, useEffect } from 'react';
import { Package, Layers, ShoppingBag, Plus, Trash2, Edit, RefreshCw, CheckCircle2, AlertCircle, X } from 'lucide-react';
import { productService, CreateProductRequest } from '../services/productService';
import { inventoryService } from '../services/inventoryService';
import { orderService } from '../services/orderService';
import { Product, Category, Order, OrderStatus } from '../types';
import { StatusBadge } from '../components/StatusBadge';

export const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'products' | 'inventory' | 'orders'>('products');

  // Products state
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [showProductModal, setShowProductModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  // Form state
  const [productForm, setProductForm] = useState<CreateProductRequest & { initialStock?: number }>({
    sku: '',
    name: '',
    description: '',
    price: 99.99,
    categoryId: 1,
    imageUrl: '',
    initialStock: 50,
  });

  // Orders state
  const [allOrders, setAllOrders] = useState<Order[]>([]);

  // Inventory state
  const [stockInputs, setStockInputs] = useState<{ [sku: string]: number }>({});

  const [loading, setLoading] = useState(false);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const [prodData, cats, orders] = await Promise.all([
        productService.getProducts({ size: 100 }),
        productService.getCategories(),
        orderService.getAllOrders().catch(() => [] as Order[]),
      ]);

      setProducts(prodData.content);
      setCategories(cats);
      setAllOrders(orders);
    } catch (err: unknown) {
      console.error('Failed to load admin data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const showFeedback = (success: string | null, error: string | null = null) => {
    setActionSuccess(success);
    setActionError(error);
    setTimeout(() => {
      setActionSuccess(null);
      setActionError(null);
    }, 4000);
  };

  // Product CRUD
  const handleOpenAdd = () => {
    setEditingProduct(null);
    setProductForm({
      sku: `SKU-${Math.floor(1000 + Math.random() * 9000)}`,
      name: '',
      description: '',
      price: 199.99,
      categoryId: categories[0]?.id || 1,
      imageUrl: '',
      initialStock: 25,
    });
    setShowProductModal(true);
  };

  const handleOpenEdit = (p: Product) => {
    setEditingProduct(p);
    setProductForm({
      sku: p.sku,
      name: p.name,
      description: p.description,
      price: p.price,
      categoryId: p.categoryId ?? p.category?.id ?? 1,
      imageUrl: p.imageUrl || '',
      initialStock: 0,
    });
    setShowProductModal(true);
  };

  const handleSaveProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingProduct) {
        await productService.updateProduct(editingProduct.id, {
          name: productForm.name,
          description: productForm.description,
          price: productForm.price,
          categoryId: productForm.categoryId,
          imageUrl: productForm.imageUrl,
        });
        showFeedback('Product updated successfully.');
      } else {
        const newProduct = await productService.createProduct({
          sku: productForm.sku,
          name: productForm.name,
          description: productForm.description,
          price: productForm.price,
          categoryId: productForm.categoryId,
          imageUrl: productForm.imageUrl,
        });

        // Initialize stock in inventory service
        if (productForm.initialStock && productForm.initialStock > 0) {
          try {
            await inventoryService.updateStock(newProduct.sku, productForm.initialStock);
          } catch (e) {
            console.error('Stock init failed', e);
          }
        }
        showFeedback(`Product ${newProduct.name} created and stock initialized.`);
      }

      setShowProductModal(false);
      loadData();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Action failed';
      showFeedback(null, msg);
    }
  };

  const handleDeleteProduct = async (id: number, name: string) => {
    if (!window.confirm(`Are you sure you want to deactivate/delete "${name}"?`)) return;
    try {
      await productService.deleteProduct(id);
      showFeedback(`Product "${name}" deleted.`);
      loadData();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Delete failed';
      showFeedback(null, msg);
    }
  };

  // Inventory Update
  const handleUpdateStock = async (sku: string) => {
    const qty = stockInputs[sku];
    if (qty === undefined || isNaN(qty) || qty < 0) {
      showFeedback(null, 'Please specify a non-negative quantity.');
      return;
    }
    try {
      await inventoryService.updateStock(sku, qty);
      showFeedback(`Stock for ${sku} updated to ${qty}.`);
      loadData();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to update stock';
      showFeedback(null, msg);
    }
  };

  // Order Status Change
  const handleOrderStatusChange = async (orderId: number, nextStatus: OrderStatus) => {
    try {
      await orderService.updateOrderStatus(orderId, nextStatus);
      showFeedback(`Order #${orderId} status updated to ${nextStatus}.`);
      loadData();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Invalid status transition';
      showFeedback(null, msg);
    }
  };

  const getValidTransitions = (current: OrderStatus): OrderStatus[] => {
    switch (current) {
      case 'PENDING':
        return ['CONFIRMED', 'CANCELLED'];
      case 'CONFIRMED':
        return ['PROCESSING', 'CANCELLED'];
      case 'PROCESSING':
        return ['SHIPPED', 'CANCELLED'];
      case 'SHIPPED':
        return ['DELIVERED'];
      default:
        return [];
    }
  };

  return (
    <div>
      <div className="flex-between" style={{ marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem',
              color: 'var(--primary)',
              fontSize: '0.8rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              marginBottom: '0.25rem',
            }}
          >
            Management Portal
          </div>
          <h1 style={{ fontSize: '1.85rem' }}>Enterprise Control Center</h1>
        </div>

        <button onClick={loadData} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} className={loading ? 'spinner' : ''} /> Refresh Telemetry
        </button>
      </div>

      {actionSuccess && (
        <div className="alert alert-success">
          <CheckCircle2 size={16} />
          <span>{actionSuccess}</span>
        </div>
      )}

      {actionError && (
        <div className="alert alert-error">
          <AlertCircle size={16} />
          <span>{actionError}</span>
        </div>
      )}

      {/* Tabs */}
      <div
        style={{
          display: 'flex',
          gap: '0.5rem',
          borderBottom: '1px solid var(--border-subtle)',
          marginBottom: '1.5rem',
        }}
      >
        <button
          onClick={() => setActiveTab('products')}
          className={`btn ${activeTab === 'products' ? 'btn-primary' : 'btn-outline'}`}
          style={{ borderRadius: 'var(--radius-sm) var(--radius-sm) 0 0' }}
        >
          <Package size={16} /> Products Catalog ({products.length})
        </button>
        <button
          onClick={() => setActiveTab('inventory')}
          className={`btn ${activeTab === 'inventory' ? 'btn-primary' : 'btn-outline'}`}
          style={{ borderRadius: 'var(--radius-sm) var(--radius-sm) 0 0' }}
        >
          <Layers size={16} /> Inventory Stock
        </button>
        <button
          onClick={() => setActiveTab('orders')}
          className={`btn ${activeTab === 'orders' ? 'btn-primary' : 'btn-outline'}`}
          style={{ borderRadius: 'var(--radius-sm) var(--radius-sm) 0 0' }}
        >
          <ShoppingBag size={16} /> Orders Stream ({allOrders.length})
        </button>
      </div>

      {/* Tab 1: Products */}
      {activeTab === 'products' && (
        <div>
          <div className="flex-between" style={{ marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.2rem' }}>Registered Products</h3>
            <button onClick={handleOpenAdd} className="btn btn-primary btn-sm">
              <Plus size={15} /> Add New Product
            </button>
          </div>

          <div className="card" style={{ padding: '0', overflow: 'hidden' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ background: 'var(--bg-surface)', borderBottom: '1px solid var(--border-subtle)' }}>
                  <th style={{ padding: '0.85rem 1rem' }}>ID / SKU</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Name</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Category</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Unit Price</th>
                  <th style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <span className="font-mono" style={{ color: '#818cf8', fontWeight: 600 }}>
                        {p.sku}
                      </span>
                    </td>
                    <td style={{ padding: '0.85rem 1rem', fontWeight: 600 }}>{p.name}</td>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <span className="admin-pill">{p.categoryName || p.category?.name || 'General'}</span>
                    </td>
                    <td style={{ padding: '0.85rem 1rem' }}>${Number(p.price).toFixed(2)}</td>
                    <td style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button
                          onClick={() => handleOpenEdit(p)}
                          className="btn btn-secondary btn-sm"
                          title="Edit Product"
                        >
                          <Edit size={13} />
                        </button>
                        <button
                          onClick={() => handleDeleteProduct(p.id, p.name)}
                          className="btn btn-danger btn-sm"
                          title="Delete Product"
                        >
                          <Trash2 size={13} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 2: Inventory */}
      {activeTab === 'inventory' && (
        <div>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Real-Time Stock Allocations</h3>
          <div className="card" style={{ padding: '0', overflow: 'hidden' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ background: 'var(--bg-surface)', borderBottom: '1px solid var(--border-subtle)' }}>
                  <th style={{ padding: '0.85rem 1rem' }}>Product SKU</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Product Name</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Set / Restock Quantity</th>
                  <th style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <span className="font-mono" style={{ color: '#818cf8', fontWeight: 600 }}>
                        {p.sku}
                      </span>
                    </td>
                    <td style={{ padding: '0.85rem 1rem' }}>{p.name}</td>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <input
                        type="number"
                        min="0"
                        placeholder="New Quantity"
                        className="form-input"
                        style={{ width: '140px', padding: '0.4rem 0.6rem' }}
                        value={stockInputs[p.sku] ?? ''}
                        onChange={(e) =>
                          setStockInputs({ ...stockInputs, [p.sku]: parseInt(e.target.value) || 0 })
                        }
                      />
                    </td>
                    <td style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>
                      <button
                        onClick={() => handleUpdateStock(p.sku)}
                        className="btn btn-primary btn-sm"
                      >
                        Update Stock
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: Orders */}
      {activeTab === 'orders' && (
        <div>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>All Platform Orders</h3>
          <div className="card" style={{ padding: '0', overflow: 'hidden' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ background: 'var(--bg-surface)', borderBottom: '1px solid var(--border-subtle)' }}>
                  <th style={{ padding: '0.85rem 1rem' }}>Order #</th>
                  <th style={{ padding: '0.85rem 1rem' }}>User ID</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Total Amount</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Current Status</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Transition Status</th>
                </tr>
              </thead>
              <tbody>
                {allOrders.map((o) => {
                  const allowed = getValidTransitions(o.status);
                  return (
                    <tr key={o.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                      <td style={{ padding: '0.85rem 1rem' }}>
                        <span className="font-mono" style={{ fontWeight: 600 }}>
                          #{o.orderNumber}
                        </span>
                      </td>
                      <td style={{ padding: '0.85rem 1rem' }}>User #{o.userId}</td>
                      <td style={{ padding: '0.85rem 1rem', fontWeight: 600 }}>${Number(o.totalAmount).toFixed(2)}</td>
                      <td style={{ padding: '0.85rem 1rem' }}>
                        <StatusBadge status={o.status} />
                      </td>
                      <td style={{ padding: '0.85rem 1rem' }}>
                        {allowed.length > 0 ? (
                          <div style={{ display: 'flex', gap: '0.4rem' }}>
                            {allowed.map((next) => (
                              <button
                                key={next}
                                onClick={() => handleOrderStatusChange(o.id, next)}
                                className="btn btn-secondary btn-sm"
                                style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }}
                              >
                                Advance to {next}
                              </button>
                            ))}
                          </div>
                        ) : (
                          <span style={{ fontSize: '0.78rem', color: 'var(--text-dim)' }}>Terminal State</span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add / Edit Product Modal */}
      {showProductModal && (
        <div className="modal-overlay" onClick={() => setShowProductModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="flex-between" style={{ marginBottom: '1.25rem' }}>
              <h2 style={{ fontSize: '1.35rem' }}>{editingProduct ? 'Edit Product' : 'Add New Product'}</h2>
              <button onClick={() => setShowProductModal(false)} className="btn btn-outline btn-sm" style={{ padding: '0.3rem' }}>
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleSaveProduct}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div className="form-group">
                  <label className="form-label">SKU</label>
                  <input
                    type="text"
                    className="form-input"
                    value={productForm.sku}
                    disabled={!!editingProduct}
                    onChange={(e) => setProductForm({ ...productForm, sku: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Category</label>
                  <select
                    className="form-select"
                    value={productForm.categoryId}
                    onChange={(e) => setProductForm({ ...productForm, categoryId: Number(e.target.value) })}
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Product Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={productForm.name}
                  onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea
                  className="form-textarea"
                  rows={3}
                  value={productForm.description}
                  onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
                  required
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div className="form-group">
                  <label className="form-label">Price ($ USD)</label>
                  <input
                    type="number"
                    step="0.01"
                    className="form-input"
                    value={productForm.price}
                    onChange={(e) => setProductForm({ ...productForm, price: parseFloat(e.target.value) })}
                    required
                  />
                </div>

                {!editingProduct && (
                  <div className="form-group">
                    <label className="form-label">Initial Inventory Units</label>
                    <input
                      type="number"
                      min="0"
                      className="form-input"
                      value={productForm.initialStock}
                      onChange={(e) => setProductForm({ ...productForm, initialStock: parseInt(e.target.value) || 0 })}
                      required
                    />
                  </div>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Image URL (Optional)</label>
                <input
                  type="url"
                  className="form-input"
                  placeholder="https://images.unsplash.com/..."
                  value={productForm.imageUrl}
                  onChange={(e) => setProductForm({ ...productForm, imageUrl: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setShowProductModal(false)} className="btn btn-outline">
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  {editingProduct ? 'Update Product' : 'Create Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
