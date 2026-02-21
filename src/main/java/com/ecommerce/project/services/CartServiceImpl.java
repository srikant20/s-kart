package com.ecommerce.project.services;

import com.ecommerce.project.dto.CartDTO;
import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.models.Cart;
import com.ecommerce.project.models.CartItem;
import com.ecommerce.project.models.Product;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtil authUtil;

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        //Find existing cart or create one
        Cart cart = createCart();
        //Retrieve product details
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //Perform validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),  productId
        );
        logger.info("Fetched Product: {}", product);
        logger.info("Fetched CartItem: {}", cartItem);

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart!");
        }

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName()+ " is not available.");
        }
        if (product.getQuantity() < quantity) {
            throw new APIException("Please order ≤ " + product.getQuantity() + " of " +
                    product.getProductName() + ".");
        }

        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // This is the critical part!
        cart.addCartItem(newCartItem);   // adds to list + sets back-reference

        //Create cart item
        logger.info("Fetched(APTC) New Cart Items: {}", newCartItem);
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        //Return updated cart
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();
        logger.info("CartItems List: {}",cartItems);

        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
                ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                map.setQuantity(item.getQuantity());
                return map;
                });
        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){
            throw new APIException("Currently, cart is empty!");
        }
        List<CartDTO> cartDTOs = carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                    List<ProductDTO> productDTOS = cart.getCartItems().stream()
                            .map(cartItem -> {
                                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                                productDTO.setQuantity(cartItem.getQuantity());
                                return productDTO;
                            }).collect(Collectors.toList());

                    cartDTO.setProducts(productDTOS);
                    return cartDTO;
                }).collect(Collectors.toList());
        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                .toList();
        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        String email = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName()+ " is not available.");
        }
        if(product.getQuantity() < quantity){
            throw new APIException("Please, make an order of the "+product.getProductName()+
                    " less than or equal to quantity "+ product.getQuantity()+".");
        }
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cartId,  productId
        );


        if(cartItem == null){
            throw new APIException("Product "+ product.getProductName()+" not available in the cart!");
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if(newQuantity < 0){
            throw new APIException("The resulting quantity cannot be negative.");
        }

        if(newQuantity > product.getQuantity()){
            throw new APIException("Please, make an order of the "+product.getProductName()+
                    " less than or equal to quantity "+ product.getQuantity()+".");
        }

        if(newQuantity == 0){
            deleteProductFromCart(cartId, productId);
        }else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);

        if(updatedItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                });
        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
        return "Product: "+ cartItem.getProduct().getProductName()+" successfully removed from cart.!";
    }

    @Transactional
    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if(cartItem == null){
            throw new APIException("Product "+ product.getProductName()+" not available in the cart!");
        }
        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }

    private Cart createCart(){

        logger.info("Fetched LoggedInEmail: {}", authUtil.loggedInEmail());
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());

        Cart newCart = cartRepository.save(cart);
        logger.info("Fetched New Cart: {}", newCart);
        return newCart;
    }


}
