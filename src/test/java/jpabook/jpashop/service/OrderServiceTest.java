package jpabook.jpashop.service;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.repository.OrderRepository;
import jpabook.jpashop.exception.NotEnoughStockException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {
    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;
    @Test
    public void 상품주문() throws Exception{
        Member member = createMember("회원");


        Book book = createBook(10,"JPAbook",10000);

        Long orderId = orderService.order(member.getId(), book.getId(), 2);

        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * 2, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.",8,book.getStockQuantity() );
    }

    private Book createBook(int stockQuantity, String name1, int price) {
        Book book = new Book();
        book.setName(name1);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울","강가","123-123"));
        em.persist(member);
        return member;
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws  Exception{
        Member member = createMember("회원2");
        Item item = createBook(10, "JPABOOK2",16000);

        int orderCount=11;

        orderService.order(member.getId(),item.getId(),orderCount);

        fail("재고 수량 부족 예외가 발생해야 한다.");

    }
    @Test
    public void 주문취소() throws Exception{
        Member member = createMember("회원3");
        Book item = createBook(10,"현수의 책",120000);

        int orderCount=2;
        Long orderId = orderService.order(member.getId(),item.getId(),orderCount);

        orderService.cancelOrder(orderId);
        Order getORder = orderRepository.findOne(orderId);

        assertEquals("주문 취소시 삳태는 cancel 이다.", OrderStatus.CANCEL, getORder.getStatus());
        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.",10,item.getStockQuantity());
    }
}