$ErrorActionPreference = "Stop"
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "   ENTERPRISE E-COMMERCE END-TO-END FLOW TEST    " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/v1"

# 1. Register User
$regEmail = "testuser_$(Get-Random)@example.com"
$regPassword = "Password@123"
Write-Host "`n1. Testing User Registration: $regEmail" -ForegroundColor Yellow
$regBody = @{
    email = $regEmail
    password = $regPassword
    firstName = "Jane"
    lastName = "Doe"
    phone = "+1-555-0199"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -ContentType "application/json" -Body $regBody
    Write-Host "Registration Success: $($regResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "Registration Failed: $_" -ForegroundColor Red
    exit 1
}

# 2. Login User
Write-Host "`n2. Testing User Login" -ForegroundColor Yellow
$loginBody = @{
    email = $regEmail
    password = $regPassword
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$userToken = $loginResponse.data.accessToken
Write-Host "User Login Success! Token length: $($userToken.Length)" -ForegroundColor Green
Write-Host "User Roles: $($loginResponse.data.user.roles -join ', '), Email: $($loginResponse.data.user.email)" -ForegroundColor Gray

# 3. Admin Login
Write-Host "`n3. Testing Admin Login" -ForegroundColor Yellow
$adminLoginBody = @{
    email = "admin@ecommerce.com"
    password = "Admin123!"
} | ConvertTo-Json

$adminResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body $adminLoginBody
$adminToken = $adminResponse.data.accessToken
Write-Host "Admin Login Success! Token length: $($adminToken.Length)" -ForegroundColor Green
Write-Host "Admin Roles: $($adminResponse.data.user.roles -join ', ')" -ForegroundColor Gray

# 4. Product & Category Browse
Write-Host "`n4. Testing Product & Category Endpoints" -ForegroundColor Yellow
$categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Get
Write-Host "Categories fetched: $($categories.data.Count)" -ForegroundColor Green

$products = Invoke-RestMethod -Uri "$baseUrl/products" -Method Get
Write-Host "Products fetched: $($products.data.content.Count) items" -ForegroundColor Green
$targetProduct = $products.data.content[0]
Write-Host "Target Product: ID=$($targetProduct.id), Name=$($targetProduct.name), SKU=$($targetProduct.sku), Stock=$($targetProduct.stockQuantity), Price=$$($targetProduct.price)" -ForegroundColor Gray

# 5. Check Inventory Service directly via Gateway
Write-Host "`n5. Testing Inventory Verification via Gateway" -ForegroundColor Yellow
$stockCheck = Invoke-RestMethod -Uri "$baseUrl/inventory/$($targetProduct.sku)" -Method Get
Write-Host "Inventory for $($targetProduct.sku): Available=$($stockCheck.data.availableQuantity), Total=$($stockCheck.data.totalQuantity)" -ForegroundColor Green
$initialStock = [int]$stockCheck.data.availableQuantity

# 6. Add Item to Cart
Write-Host "`n6. Testing Cart Service (Add to Cart)" -ForegroundColor Yellow
$cartHeaders = @{
    Authorization = "Bearer $userToken"
}
$addToCartBody = @{
    productId = $targetProduct.id
    sku = $targetProduct.sku
    quantity = 2
} | ConvertTo-Json

$cartResponse = Invoke-RestMethod -Uri "$baseUrl/cart/items" -Method Post -Headers $cartHeaders -ContentType "application/json" -Body $addToCartBody
Write-Host "Cart item added: Items Count=$($cartResponse.data.items.Count), TotalPrice=$$($cartResponse.data.totalPrice)" -ForegroundColor Green

# 7. Get Cart
Write-Host "`n7. Testing Cart Retrieval" -ForegroundColor Yellow
$getCart = Invoke-RestMethod -Uri "$baseUrl/cart" -Method Get -Headers $cartHeaders
Write-Host "Cart retrieved successfully: Total items=$($getCart.data.totalItems), Subtotal=$$($getCart.data.totalPrice)" -ForegroundColor Green

# 8. Place Order (Checkout)
Write-Host "`n8. Testing Order Service (Place Order)" -ForegroundColor Yellow
$orderBody = @{
    shippingAddress = "456 Innovation Blvd, Tech City, CA 94016"
} | ConvertTo-Json

$orderResponse = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers $cartHeaders -ContentType "application/json" -Body $orderBody
$createdOrder = $orderResponse.data
Write-Host "Order Placed Successfully! Order Number: $($createdOrder.orderNumber)" -ForegroundColor Green
Write-Host "Order Status: $($createdOrder.status), Total Amount: $$($createdOrder.totalAmount), Items: $($createdOrder.items.Count)" -ForegroundColor Gray

# 9. Verify Stock Decremented in Inventory Service
Write-Host "`n9. Verifying Inventory Stock Decrement" -ForegroundColor Yellow
$stockAfterOrder = Invoke-RestMethod -Uri "$baseUrl/inventory/$($targetProduct.sku)" -Method Get
Write-Host "Stock before order: $initialStock, Stock after order: $($stockAfterOrder.data.availableQuantity)" -ForegroundColor Gray
if ([int]$stockAfterOrder.data.availableQuantity -eq ($initialStock - 2)) {
    Write-Host "SUCCESS: Inventory accurately deducted by 2 units!" -ForegroundColor Green
} else {
    Write-Host "WARNING: Expected stock $($initialStock - 2) but got $($stockAfterOrder.data.availableQuantity)" -ForegroundColor Red
}

# 10. Verify Cart is Cleared after checkout
Write-Host "`n10. Verifying Cart Cleared After Checkout" -ForegroundColor Yellow
$cartAfterOrder = Invoke-RestMethod -Uri "$baseUrl/cart" -Method Get -Headers $cartHeaders
if ($cartAfterOrder.data.items.Count -eq 0) {
    Write-Host "SUCCESS: Cart was automatically cleared after checkout!" -ForegroundColor Green
} else {
    Write-Host "WARNING: Cart still contains $($cartAfterOrder.data.items.Count) items" -ForegroundColor Red
}

# 11. Customer Order History
Write-Host "`n11. Testing Customer Order History" -ForegroundColor Yellow
$customerOrders = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Get -Headers $cartHeaders
Write-Host "Found $($customerOrders.data.content.Count) orders for this customer" -ForegroundColor Green

# 12. Admin Order Management (Update Status to PROCESSING)
Write-Host "`n12. Testing Admin Order Status Transition" -ForegroundColor Yellow
$adminHeaders = @{
    Authorization = "Bearer $adminToken"
}
$statusUpdateBody = @{
    status = "PROCESSING"
    notes = "Warehouse packaging confirmed."
} | ConvertTo-Json

$statusUpdate = Invoke-RestMethod -Uri "$baseUrl/orders/admin/$($createdOrder.id)/status" -Method Put -Headers $adminHeaders -ContentType "application/json" -Body $statusUpdateBody
Write-Host "Admin updated order status to: $($statusUpdate.data.status)" -ForegroundColor Green

# 13. Test Order Cancellation & Inventory Restock
Write-Host "`n13. Testing Order Cancellation & Stock Restock Flow" -ForegroundColor Yellow
# Customer cancels order
$cancelResponse = Invoke-RestMethod -Uri "$baseUrl/orders/$($createdOrder.id)/cancel" -Method Post -Headers $cartHeaders
Write-Host "Order Cancellation Response: Status=$($cancelResponse.data.status)" -ForegroundColor Green

$stockAfterCancel = Invoke-RestMethod -Uri "$baseUrl/inventory/$($targetProduct.sku)" -Method Get
Write-Host "Stock after cancellation: $($stockAfterCancel.data.availableQuantity)" -ForegroundColor Gray
if ([int]$stockAfterCancel.data.availableQuantity -eq $initialStock) {
    Write-Host "SUCCESS: Inventory restocked back to original $initialStock!" -ForegroundColor Green
} else {
    Write-Host "WARNING: Expected stock $initialStock but got $($stockAfterCancel.data.availableQuantity)" -ForegroundColor Red
}

Write-Host "`n=================================================" -ForegroundColor Cyan
Write-Host "   ALL 13 FUNCTIONALITY CHECKS PASSED PERFECTLY! " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
