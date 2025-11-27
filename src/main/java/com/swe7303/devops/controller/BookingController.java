package com.swe7303.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.swe7303.devops.model.Booking;
import com.swe7303.devops.service.BookingService;
import com.swe7303.devops.service.PackageService;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PackageService packageService;

    // READ - List all bookings
    @GetMapping("/bookings")
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "bookings";
    }

    // CREATE - Show add form
    @GetMapping("/add-booking")
    public String addBookingForm(Model model) {
        model.addAttribute("booking", new Booking());
        model.addAttribute("packages", packageService.getAllPackages());
        return "add_booking";
    }

    // CREATE - Save new booking
    @PostMapping("/bookings")
    public String saveBooking(@ModelAttribute("booking") Booking booking) {
        bookingService.saveBooking(booking);
        return "redirect:/bookings";
    }

    // UPDATE - Show edit form
    @GetMapping("/edit-booking/{id}")
    public String editBookingForm(@PathVariable("id") Integer id, Model model) {
        Booking booking = bookingService.getBookingById(id);
        model.addAttribute("booking", booking);
        model.addAttribute("packages", packageService.getAllPackages());
        return "edit_booking";
    }

    // UPDATE - Save edited booking
    @PostMapping("/update-booking/{id}")
    public String updateBooking(@PathVariable("id") Integer id,
            @ModelAttribute("booking") Booking booking) {
        booking.setBookingId(id);
        bookingService.saveBooking(booking);
        return "redirect:/bookings";
    }

    // DELETE - Remove booking
    @GetMapping("/delete-booking/{id}")
    public String deleteBooking(@PathVariable("id") Integer id) {
        bookingService.deleteBooking(id);
        return "redirect:/bookings";
    }
}
