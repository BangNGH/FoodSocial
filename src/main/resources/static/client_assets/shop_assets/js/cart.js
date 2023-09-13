function formatPrice(price) {
    return price.toLocaleString('en-US', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }) + ' VNĐ';
}
function addToCart(productID) {
    $.ajax({
        url: '/getProduct/' + productID,
        type: 'GET',
        success: function (data) {
            let details = "/shop/detail/" + productID;
            let quantity = $('#quantity').val();
            if(quantity == undefined)
                quantity = 1;
            //show success
            $("#modalProductName").text(data.name);
            $("#modalProductName").attr('href', details)
            $("#modalProductImg").attr('src', data.image);
            //add to cart
            $.ajax({
                url: '/add-to-cart/' + productID + "/" + quantity,
                type: 'GET',
                success: function () {

                }
            });
        }
    });
}
function getTotalPrice(){
    $.ajax({
        url: '/getTotalPrice',
        type: 'GET',
        success: function (data) {
            $('#totalPrice').text(formatPrice(data));
            $('#totalPrice2').text(formatPrice(data));
        }
    });
}
function removeItem(productID){
    $.ajax({
        url: '/removeItem/' + productID,
        type: 'GET',
        success: function () {
            getCart();
        }
    });
}
function updateItem(productID, element){
    var quantity = $(element).val();
    $.ajax({
        url: '/updateCart/' + productID + '/' + quantity,
        type: 'GET',
        success: function () {
            getCart();
        }
    });
}
function getCart(){
    $.ajax({
        url: '/getCart',
        type: 'GET',
        success: function (data) {
            if(data.length > 0){
                $('#cart').empty();
                $('#cart-footer').empty()
                let cartHeaderHTML = '';
                cartHeaderHTML =  cartHeaderHTML + '<div class="table-responsive">\n' +
                    '                            <table class="table align-middle table-nowrap table-check">\n' +
                    '                                <thead class="table-light">\n' +
                    '                                <th class="align-middle">Xóa</th>\n' +
                    '                                <th class="align-middle cart-product-image">Ảnh</th>\n' +
                    '                                <th class="align-middle">Sản phẩm</th>\n' +
                    '                                <th class="align-middle">Giá</th>\n' +
                    '                                <th class="align-middle">Số lượng</th>\n' +
                    '                                <th class="align-middle">Tổng</th>\n' +
                    '                                </thead>\n' +
                    '                                <tbody id="cart-body">\n' +
                    '\n' +
                    '                                </tbody>\n' +
                    '                            </table>\n' +
                    '                        </div>';
                let cartFooterHTML = '';
                cartFooterHTML = cartFooterHTML +'<h4>Cart Totals</h4>\n' +
                    '                            <table class="table">\n' +
                    '                                <tbody>\n' +
                    '                                <tr>\n' +
                    '                                    <td>Tổng giỏ hàng</td>\n' +
                    '                                    <td id="totalPrice"></td>\n' +
                    '                                </tr>\n' +
                    '                                <tr>\n' +
                    '                                    <td>Giao hàng và sử lí</td>\n' +
                    '                                    <td>Free</td>\n' +
                    '                                </tr>\n' +
                    '                                <tr>\n' +
                    '                                    <td><strong>Tổng giỏ hàng</strong></td>\n' +
                    '                                    <td><strong>\n' +
                    '                                        <span id="totalPrice2"></span>\n' +
                    '                                    </strong></td>\n' +
                    '                                </tr>\n' +
                    '                                </tbody>\n' +
                    '                            </table>\n' +
                    '                            <div class="btn-wrapper text-right text-end">\n' +
                    '                                <a href="/cart/checkout" class="theme-btn-1 btn btn-effect-1">Tiến hành thanh toán</a>\n' +
                    '                            </div>';
                let cartBodyHTML = '';
                $.each(data, function(i, item){
                    cartBodyHTML += '<tr>' +
                        '<td class="cart-product-remove"><a onclick="removeItem(' + item.productId + ');">X</a></td>' +
                        '<td class="cart-product-image">' +
                        '<a thref="/shop/detail/' + item.productId + '"><img src="' + item.img + '" alt="#"></a>' +
                        '</td>' +
                        '<td class="cart-product-info">' +
                        '<h4 style="font-size: 16px"><a href="/shop/detail/' + item.productId + '">' + item.productName + '</a></h4>' +
                        '</td>' +
                        '<td class="cart-product-price"><span>'+ formatPrice(item.price) +'</span></td>' +
                        '<td class="cart-product-quantity">' +
                        '<div class="cart-product-quantity">' +
                        '<input style="border: none" type="number" min="1" value="' + item.quantity + '" onclick="updateItem(' + item.productId + ', this);" name="qtybutton" class="quantity">' +
                        '</div>' +
                        '</td>' +
                        '<td class="cart-product-subtotal">' +
                        '<span>'+ formatPrice(item.price * item.quantity) +'</span>' +
                        '</td>' +
                        '</tr>';
                });
                $('#cart').append(cartHeaderHTML);
                $('#cart-body').append(cartBodyHTML);
                $('#cart-footer').append(cartFooterHTML);
                getTotalPrice();
            }
            if (data.length === 0) {
                $('#cart').empty();
                $('#cart-footer').empty()
                let cartEmptyHTML = '';
                cartEmptyHTML = cartEmptyHTML + '<h3 class="mt-5">Giỏ hàng của bạn hiện đang trống</h3>\n' +
                    '                        <p class="lead">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>\n' +
                    '                        <a class="theme-btn-1 btn btn-effect-1" href="/shop">Đến cửa hàng</a>';
                $('#cart').append(cartEmptyHTML);
            }
        }
    });
}
function quickView(productID) {
    $.ajax({
        url: '/getProduct/' + productID,
        type: 'GET',
        dataType: 'json',
        success: function (data) {
            $("#QVName").text(data.name);
            $("#QVPrice").text(formatPrice(data.price));
            $("#QVImg").attr('src',data.image);
            let QVAddToCart ='';
            let QVWishList = '';
            $('#QVAddToCart').empty();
                QVAddToCart = QVAddToCart + ' <a href="#" class="theme-btn-1 btn btn-effect-1"\n' +
                    '                                                               title="Add to Cart" data-bs-toggle="modal"\n' +
                    '                                                               data-bs-target="#add_to_cart_modal" onclick="addToCartQV(\''+data.id+'\',)">\n' +
                    '                                                                <i class="fas fa-shopping-cart"></i>\n' +
                    '                                                                <span>ADD TO CART</span>\n' +
                    '                                                            </a>';
            // else if(data.quantity <= 1) {
            //     QVAddToCart = QVAddToCart + ' <a href="#" class="theme-btn-1 btn btn-effect-1"\n' +
            //         '                                                               title="Out of stock"\n' +
            //         '                                                               >\n' +
            //         '                                                                <i class="fas fa-shopping-cart"></i>\n' +
            //         '                                                                <span style="opacity: 0.8">OUT OF STOCK</span>\n' +
            //         '                                                            </a>';
            // }

            // QVWishList = QVWishList + ' <a href="#" class="" title="Wishlist" data-bs-toggle="modal"\n' +
            //     '                                                               data-bs-target="#liton_wishlist_modal" onclick="addToWishListQV(\''+data.id+'\',)">\n' +
            //     '                                                                <i class="far fa-heart"></i>\n' +
            //     '                                                                <span>Add to Wishlist</span>\n' +
            //     '                                                            </a>';

            $("#QVAddToCart").html(QVAddToCart);
            // $("#QVWishlist").html(QVWishList);
            $('.modal-backdrop.fade.show:first').remove();
        }
    });
}
function addToCartQV(productId){
    $.ajax({
        url: '/getProduct/' + productId,
        type: 'GET',
        dataType: 'json',
        success: function (data) {
            //show modal add to cart
            let details = "/product/detail/" + productId;
            $("#modalProductName").text(data.name);
            $("#modalProductName").attr('href', details)
            $("#modalProductImg").attr('src', data.image);
            let quantity = $("#quantityQV").val();
            //add to cart
            $.ajax({
                url: '/add-to-cart/' +productId + "/" + quantity,
                type: 'GET',
                success: function () {

                }
            });
        }
    });
}