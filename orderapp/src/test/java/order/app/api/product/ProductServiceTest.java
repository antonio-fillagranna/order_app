package order.app.api.product;

import static order.app.api.product.ProductCategory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProductById_ProductExists() {
        Product product = new Product("Product 1", 100.0, "Description 1", SASHIMIS);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product foundProduct = productService.findById(1L);

        assertNotNull(foundProduct);
        assertEquals(1L, foundProduct.getId());
        assertEquals("Product 1", foundProduct.getName());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_ProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.findById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Produto não encontrado", exception.getReason());

        verify(productRepository, times(1)).findById(1L);
    }



    @Test
    void testCreateProduct() {
        DTOProductRegistry dto = new DTOProductRegistry("Product 1", 100.0, "Description 1", ENTRADAS);
        Product product = new Product(dto.name(), dto.price(), dto.description(), dto.category());

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product createdProduct = productService.createProduct(dto);

        assertNotNull(createdProduct);
        assertEquals(dto.name(), createdProduct.getName());
        assertEquals(dto.price(), createdProduct.getPrice());
        assertEquals(dto.description(), createdProduct.getDescription());
        assertEquals(dto.category(), createdProduct.getCategory());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetProductById() {
        Long id = 1L;
        Product product = new Product("Product 1", 100.0, "Description 1", SUSHIS);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.getProductById(id);

        assertTrue(foundProduct.isPresent());
        assertEquals(product, foundProduct.get());

        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Product 1", 100.0, "Description 1", SASHIMIS));
        products.add(new Product("Product 2", 200.0, "Description 2", COMBINADOS));

        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> allProducts = productService.getAllProducts(pageable);

        assertNotNull(allProducts);
        assertEquals(2, allProducts.getTotalElements());

        assertEquals(products, allProducts.getContent());

        verify(productRepository, times(1)).findAll(pageable);
    }


    @Test
    void testUpdateProduct() {
        Long id = 1L;
        Product existingProduct = new Product("Product 1", 100.0, "Description 1", HOT_ROLLS_E_PRATOS_QUENTES);
        DTOUpdateProduct dto = new DTOUpdateProduct();
        dto.setPrice(200.0);
        dto.setDescription("Updated Description");
        dto.setCategory(URAMAKIS);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean isUpdated = productService.updateProduct(id, dto);

        assertTrue(isUpdated);
        assertEquals(dto.getPrice(), existingProduct.getPrice());
        assertEquals(dto.getDescription(), existingProduct.getDescription());
        assertEquals(dto.getCategory(), existingProduct.getCategory());

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void testUpdateProductNotFound() {
        Long id = 1L;
        DTOUpdateProduct dto = new DTOUpdateProduct();
        dto.setPrice(200.0);
        dto.setDescription("Updated Description");
        dto.setCategory(HOSSOMAKIS);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        boolean isUpdated = productService.updateProduct(id, dto);

        assertFalse(isUpdated);

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(0)).save(any(Product.class));
    }

    @Test
    void testUpdateProductAllFields() {
        Long id = 1L;
        Product existingProduct = new Product("Product 1", 100.0, "Description 1", NIGUIRIS);
        DTOUpdateProduct dto = new DTOUpdateProduct();
        dto.setPrice(200.0);
        dto.setDescription("Updated Description");
        dto.setCategory(TEMAKIS);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean isUpdated = productService.updateProduct(id, dto);

        assertTrue(isUpdated);
        assertEquals(dto.getPrice(), existingProduct.getPrice());
        assertEquals(dto.getDescription(), existingProduct.getDescription());
        assertEquals(dto.getCategory(), existingProduct.getCategory());

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void testUpdateProductPriceOnly() {
        Long id = 1L;
        Product existingProduct = new Product("Product 1", 100.0, "Description 1", FUTOMAKIS);
        DTOUpdateProduct dto = new DTOUpdateProduct();
        dto.setPrice(200.0);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean isUpdated = productService.updateProduct(id, dto);

        assertTrue(isUpdated);
        assertEquals(dto.getPrice(), existingProduct.getPrice());
        assertEquals("Description 1", existingProduct.getDescription());
        assertEquals(FUTOMAKIS, existingProduct.getCategory());

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void testUpdateProductDescriptionOnly() {
        Long id = 1L;
        Product existingProduct = new Product("Product 1", 100.0, "Description 1", GUNKAN_E_ACELGAMAKI);
        DTOUpdateProduct dto = new DTOUpdateProduct();
        dto.setDescription("Updated Description");

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean isUpdated = productService.updateProduct(id, dto);

        assertTrue(isUpdated);
        assertEquals(100.0, existingProduct.getPrice());
        assertEquals(dto.getDescription(), existingProduct.getDescription());
        assertEquals(GUNKAN_E_ACELGAMAKI, existingProduct.getCategory());

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void testUpdateProductCategoryOnly() {
        Long id = 1L;
        Product existingProduct = new Product("Product 1", 100.0, "Description 1", SOBREMESAS);
        DTOUpdateProduct dto = new DTOUpdateProduct();
        dto.setCategory(BEBIDAS);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean isUpdated = productService.updateProduct(id, dto);

        assertTrue(isUpdated);
        assertEquals(100.0, existingProduct.getPrice());
        assertEquals("Description 1", existingProduct.getDescription());
        assertEquals(dto.getCategory(), existingProduct.getCategory());

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void testDeleteProduct() {
        Long id = 1L;

        when(productRepository.existsById(id)).thenReturn(true);
        doNothing().when(productRepository).deleteById(id);

        boolean isDeleted = productService.deleteProduct(id);

        assertTrue(isDeleted);

        verify(productRepository, times(1)).existsById(id);
        verify(productRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteProductNotFound() {
        Long id = 1L;

        when(productRepository.existsById(id)).thenReturn(false);

        boolean isDeleted = productService.deleteProduct(id);

        assertFalse(isDeleted);

        verify(productRepository, times(1)).existsById(id);
        verify(productRepository, times(0)).deleteById(id);
    }
}
