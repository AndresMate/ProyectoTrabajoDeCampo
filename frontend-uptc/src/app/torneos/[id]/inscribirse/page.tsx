"use client";
import React from "react";
import { useParams, useRouter } from "next/navigation";
import InscriptionCompleteForm from "@/components/forms/InscriptionCompleteForm";

export default function InscripcionPage() {
  const { id } = useParams();
  const router = useRouter();

  const handleSuccess = () => {
    alert("Inscripción completada con éxito ✅");
    router.push("/torneos");
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <InscriptionCompleteForm
        tournamentId={Number(id)}
        onSuccess={handleSuccess}
        onCancel={() => router.push("/torneos")}
      />
    </div>
  );
}
